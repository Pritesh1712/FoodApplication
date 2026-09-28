const Restaurant = require('../models/Restaurant');

// @desc    Get all restaurants
// @route   GET /api/restaurants
// @access  Public
exports.getRestaurants = async (req, res) => {
  try {
    const restaurants = await Restaurant.find({ isApproved: true, isActive: true });
    res.json({ success: true, count: restaurants.length, data: restaurants });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Get single restaurant
// @route   GET /api/restaurants/:id
// @access  Public
exports.getRestaurantById = async (req, res) => {
  try {
    const restaurant = await Restaurant.findById(req.params.id);
    if (!restaurant) {
      return res.status(404).json({ success: false, message: 'Restaurant not found' });
    }
    res.json({ success: true, data: restaurant });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Get logged in user's restaurant
// @route   GET /api/restaurants/my/restaurant
// @access  Private (Restaurant Owner)
exports.getMyRestaurant = async (req, res) => {
  try {
    let restaurant = await Restaurant.findOne({ owner: req.user._id });
    if (!restaurant) {
      // Auto-create default restaurant for owner if not exists
      restaurant = await Restaurant.create({
        owner: req.user._id,
        name: req.user.name + "'s Kitchen",
        description: 'Quality campus food',
        address: 'Campus Food Court',
        phone: req.user.phone || '9876543210',
        rating: 4.5,
        isApproved: true,
        isActive: true
      });
    }
    res.json({ success: true, data: restaurant });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Create/Update restaurant
// @route   POST /api/restaurants
// @access  Private (Restaurant Owner / Admin)
exports.createRestaurant = async (req, res) => {
  try {
    const { name, description, address, phone, image } = req.body;

    let restaurant = await Restaurant.findOne({ owner: req.user._id });

    if (restaurant) {
      restaurant.name = name || restaurant.name;
      restaurant.description = description || restaurant.description;
      restaurant.address = address || restaurant.address;
      restaurant.phone = phone || restaurant.phone;
      restaurant.image = image || restaurant.image;

      const updated = await restaurant.save();
      return res.json({ success: true, data: updated });
    }

    restaurant = await Restaurant.create({
      owner: req.user._id,
      name,
      description: description || '',
      address,
      phone,
      image: image || ''
    });

    res.status(201).json({ success: true, data: restaurant });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
