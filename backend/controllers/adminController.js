const User = require('../models/User');
const Restaurant = require('../models/Restaurant');
const Food = require('../models/Food');
const Category = require('../models/Category');
const Order = require('../models/Order');

// @desc    Get all users
// @route   GET /api/admin/users
// @access  Private (Admin)
exports.getAllUsers = async (req, res) => {
  try {
    const users = await User.find({}).select('-password').sort({ createdAt: -1 });
    res.json({ success: true, count: users.length, data: users });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Delete user
// @route   DELETE /api/admin/users/:id
// @access  Private (Admin)
exports.deleteUser = async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    if (user.role === 'RESTAURANT') {
      const rest = await Restaurant.findOne({ owner: user._id });
      if (rest) {
        await Food.deleteMany({ restaurant: rest._id });
        await Category.deleteMany({ restaurant: rest._id });
        await Order.deleteMany({ restaurant: rest._id });
        await Restaurant.findByIdAndDelete(rest._id);
      }
    }

    await User.findByIdAndDelete(req.params.id);
    res.json({ success: true, message: 'User deleted successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Delete restaurant with cascading deletion of products, categories & orders
// @route   DELETE /api/admin/restaurants/:id
// @access  Private (Admin)
exports.deleteRestaurant = async (req, res) => {
  try {
    const restaurantId = req.params.id;
    const restaurant = await Restaurant.findById(restaurantId);
    if (!restaurant) {
      return res.status(404).json({ success: false, message: 'Restaurant not found' });
    }

    // 1. Delete all products belonging to this shop
    await Food.deleteMany({ restaurant: restaurantId });

    // 2. Delete all custom categories created for this shop
    await Category.deleteMany({ restaurant: restaurantId });

    // 3. Delete all orders placed for this shop
    await Order.deleteMany({ restaurant: restaurantId });

    // 4. Delete restaurant owner user account if exists
    if (restaurant.owner) {
      await User.findByIdAndDelete(restaurant.owner);
    }

    // 5. Delete restaurant document
    await Restaurant.findByIdAndDelete(restaurantId);

    res.json({
      success: true,
      message: 'Restaurant, products, categories, and owner account deleted successfully'
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Toggle restaurant approval
// @route   PUT /api/admin/restaurants/:id/approve
// @access  Private (Admin)
exports.toggleRestaurantStatus = async (req, res) => {
  try {
    const restaurant = await Restaurant.findById(req.params.id);
    if (!restaurant) {
      return res.status(404).json({ success: false, message: 'Restaurant not found' });
    }

    restaurant.isApproved = !restaurant.isApproved;
    await restaurant.save();

    res.json({ success: true, data: restaurant });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Get admin statistics
// @route   GET /api/admin/stats
// @access  Private (Admin)
exports.getAdminStats = async (req, res) => {
  try {
    const totalUsers = await User.countDocuments();
    const totalRestaurants = await Restaurant.countDocuments();
    const totalOrders = await Order.countDocuments();

    res.json({
      success: true,
      data: {
        totalUsers,
        totalRestaurants,
        totalOrders
      }
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
