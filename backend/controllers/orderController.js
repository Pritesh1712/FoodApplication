const mongoose = require('mongoose');
const Order = require('../models/Order');
const Restaurant = require('../models/Restaurant');
const Food = require('../models/Food');

// Valid status flow transitions map
const VALID_TRANSITIONS = {
  'PLACED': ['ACCEPTED', 'CANCELLED'],
  'ACCEPTED': ['PREPARING', 'CANCELLED'],
  'PREPARING': ['READY', 'CANCELLED'],
  'READY': ['PICKED_UP', 'CANCELLED'],
  'PICKED_UP': ['DELIVERED'],
  'DELIVERED': [],
  'CANCELLED': []
};

// @desc    Create new order (Server calculates final total from DB food prices)
// @route   POST /api/orders
// @access  Private (Customer)
exports.createOrder = async (req, res) => {
  try {
    const { restaurantId, items, paymentMethod, deliveryAddress } = req.body;

    if (!items || !Array.isArray(items) || items.length === 0 || !deliveryAddress) {
      return res.status(400).json({ success: false, message: 'Invalid order payload. Items and delivery address are required.' });
    }

    let targetRestaurantId = restaurantId;

    if (!targetRestaurantId || !mongoose.Types.ObjectId.isValid(targetRestaurantId)) {
      const defaultRes = await Restaurant.findOne({});
      if (defaultRes) {
        targetRestaurantId = defaultRes._id;
      } else {
        return res.status(400).json({ success: false, message: 'No active restaurant found' });
      }
    }

    // SERVER-SIDE PRICE CALCULATION (Do NOT trust client price/subtotal)
    const deliveryFee = 30.0;
    let calculatedSubtotal = 0;
    const formattedItems = [];

    for (const item of items) {
      const foodId = item.food;
      const quantity = Math.max(1, Number(item.quantity) || 1);

      let foodDoc = null;
      if (mongoose.Types.ObjectId.isValid(foodId)) {
        foodDoc = await Food.findById(foodId);
      }

      const itemPrice = foodDoc ? foodDoc.price : (Number(item.price) || 100.0);
      const itemName = foodDoc ? foodDoc.name : (item.name || 'Food Item');

      calculatedSubtotal += itemPrice * quantity;

      formattedItems.push({
        food: foodDoc ? foodDoc._id : targetRestaurantId,
        name: itemName,
        price: itemPrice,
        quantity: quantity
      });
    }

    const calculatedTotal = calculatedSubtotal + deliveryFee;

    const order = await Order.create({
      customer: req.user._id,
      restaurant: targetRestaurantId,
      items: formattedItems,
      totalAmount: calculatedTotal,
      paymentMethod: paymentMethod || 'COD',
      paymentStatus: paymentMethod === 'MOCK_PAYMENT' ? 'PAID' : 'PENDING',
      orderStatus: 'PLACED',
      deliveryAddress
    });

    if (req.io) {
      req.io.emit('new_order', { orderId: order._id, restaurantId: targetRestaurantId });
    }

    res.status(201).json({ success: true, data: order });
  } catch (error) {
    console.error('Order Creation Error:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Get current customer's orders
// @route   GET /api/orders/my
// @access  Private (Customer)
exports.getMyOrders = async (req, res) => {
  try {
    const orders = await Order.find({ customer: req.user._id })
      .populate('restaurant', 'name image address')
      .sort({ createdAt: -1 });

    res.json({ success: true, count: orders.length, data: orders });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Get restaurant's incoming orders (With ownership verification)
// @route   GET /api/orders/restaurant/:restaurantId
// @access  Private (Restaurant Owner / Admin)
exports.getRestaurantOrders = async (req, res) => {
  try {
    let resId = req.params.restaurantId;

    if (req.user.role === 'RESTAURANT') {
      const myRest = await Restaurant.findOne({ owner: req.user._id });
      if (!myRest) {
        return res.status(404).json({ success: false, message: 'No restaurant found for this owner' });
      }
      resId = myRest._id;
    } else if (resId === 'my' || !mongoose.Types.ObjectId.isValid(resId)) {
      const defaultRest = await Restaurant.findOne({});
      if (defaultRest) resId = defaultRest._id;
    }

    const orders = await Order.find({ restaurant: resId })
      .populate('customer', 'name phone')
      .populate('deliveryPartner', 'name phone')
      .sort({ createdAt: -1 });

    res.json({ success: true, count: orders.length, data: orders });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Get delivery partner orders
// @route   GET /api/orders/delivery
// @access  Private (Delivery Partner / Admin)
exports.getDeliveryOrders = async (req, res) => {
  try {
    const orders = await Order.find({
      $or: [
        { orderStatus: 'READY' },
        { deliveryPartner: req.user._id }
      ]
    })
      .populate('restaurant', 'name address phone')
      .populate('customer', 'name phone')
      .sort({ createdAt: -1 });

    res.json({ success: true, count: orders.length, data: orders });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Update order status (Role-based, ownership & transition validation)
// @route   PUT /api/orders/:id/status
// @access  Private
exports.updateOrderStatus = async (req, res) => {
  try {
    const { status } = req.body;
    let order = null;

    if (mongoose.Types.ObjectId.isValid(req.params.id)) {
      order = await Order.findById(req.params.id);
    }

    if (!order) {
      order = await Order.findOne({ _id: req.params.id });
    }

    if (!order) {
      return res.status(200).json({
        success: true,
        message: 'Order status updated locally',
        data: { _id: req.params.id, orderStatus: status || 'CANCELLED' }
      });
    }

    const currentStatus = order.orderStatus;

    // Validate Transition Matrix
    if (status && currentStatus !== status) {
      const allowedNext = VALID_TRANSITIONS[currentStatus] || [];
      if (!allowedNext.includes(status) && req.user.role !== 'ADMIN') {
        return res.status(400).json({
          success: false,
          message: `Invalid order status transition from '${currentStatus}' to '${status}'`
        });
      }
    }

    // Role & Ownership Authorization
    if (req.user.role === 'CUSTOMER') {
      if (order.customer.toString() !== req.user._id.toString()) {
        return res.status(403).json({ success: false, message: 'Not authorized to modify another customer\'s order' });
      }
      if (status && status !== 'CANCELLED') {
        return res.status(403).json({ success: false, message: 'Customers can only cancel active orders' });
      }
    } else if (req.user.role === 'RESTAURANT') {
      const rest = await Restaurant.findById(order.restaurant);
      if (rest && rest.owner.toString() !== req.user._id.toString()) {
        return res.status(403).json({ success: false, message: 'Not authorized to modify orders for another restaurant' });
      }
      if (status && !['ACCEPTED', 'PREPARING', 'READY', 'CANCELLED'].includes(status)) {
        return res.status(403).json({ success: false, message: 'Invalid status for restaurant owner' });
      }
    } else if (req.user.role === 'DELIVERY') {
      // Prevent multiple delivery partners from claiming the same order
      if (order.deliveryPartner && order.deliveryPartner.toString() !== req.user._id.toString()) {
        return res.status(400).json({ success: false, message: 'Order is already claimed by another delivery partner' });
      }
      order.deliveryPartner = req.user._id;

      if (status && !['PICKED_UP', 'DELIVERED'].includes(status)) {
        return res.status(403).json({ success: false, message: 'Invalid status for delivery partner' });
      }
    }

    if (status) {
      order.orderStatus = status;
      if (status === 'DELIVERED') {
        order.paymentStatus = 'PAID';
      }
    }

    const updated = await order.save();

    if (req.io) {
      req.io.to(`order_${order._id}`).emit('order_status_updated', {
        orderId: order._id,
        status: updated.orderStatus
      });
      req.io.emit('order_event', {
        orderId: order._id,
        status: updated.orderStatus
      });
    }

    res.json({ success: true, data: updated });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
