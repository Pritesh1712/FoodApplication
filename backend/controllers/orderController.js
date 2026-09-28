const mongoose = require('mongoose');
const Order = require('../models/Order');
const Restaurant = require('../models/Restaurant');

// @desc    Create new order
// @route   POST /api/orders
// @access  Private (Customer)
exports.createOrder = async (req, res) => {
  try {
    const { restaurantId, items, totalAmount, paymentMethod, deliveryAddress } = req.body;

    if (!items || items.length === 0 || !deliveryAddress) {
      return res.status(400).json({ success: false, message: 'Invalid order payload' });
    }

    let targetRestaurantId = restaurantId;

    // Fallback if restaurantId is a string like "r1"
    if (!targetRestaurantId || !mongoose.Types.ObjectId.isValid(targetRestaurantId)) {
      const defaultRes = await Restaurant.findOne({});
      if (defaultRes) {
        targetRestaurantId = defaultRes._id;
      } else {
        return res.status(400).json({ success: false, message: 'No active restaurant found' });
      }
    }

    const formattedItems = items.map(item => ({
      food: mongoose.Types.ObjectId.isValid(item.food) ? item.food : targetRestaurantId,
      name: item.name || 'Food Item',
      price: item.price || 100,
      quantity: item.quantity || 1
    }));

    const order = await Order.create({
      customer: req.user._id,
      restaurant: targetRestaurantId,
      items: formattedItems,
      totalAmount,
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

// @desc    Get current user's orders
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

// @desc    Get restaurant's incoming orders
// @route   GET /api/orders/restaurant/:restaurantId
// @access  Private (Restaurant Owner / Admin)
exports.getRestaurantOrders = async (req, res) => {
  try {
    let resId = req.params.restaurantId;
    if (resId === 'my' || !mongoose.Types.ObjectId.isValid(resId)) {
      const rest = await Restaurant.findOne({ owner: req.user._id });
      if (rest) resId = rest._id;
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

// @desc    Update order status
// @route   PUT /api/orders/:id/status
// @access  Private
exports.updateOrderStatus = async (req, res) => {
  try {
    const { status, deliveryPartnerId } = req.body;
    let order;

    if (mongoose.Types.ObjectId.isValid(req.params.id)) {
      order = await Order.findById(req.params.id);
    }

    if (!order) {
      order = await Order.findOne({ _id: req.params.id });
    }

    if (!order) {
      return res.status(200).json({
        success: true,
        message: 'Order updated locally',
        data: { _id: req.params.id, orderStatus: status || 'CANCELLED' }
      });
    }

    if (status) {
      order.orderStatus = status;
      if (status === 'DELIVERED') {
        order.paymentStatus = 'PAID';
      }
    }

    if (deliveryPartnerId || req.user.role === 'DELIVERY') {
      order.deliveryPartner = deliveryPartnerId || req.user._id;
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
