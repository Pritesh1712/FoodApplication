const Food = require('../models/Food');
const Restaurant = require('../models/Restaurant');

// @desc    Get foods by restaurant ID
// @route   GET /api/food/restaurant/:restaurantId
// @access  Public
exports.getFoodsByRestaurant = async (req, res) => {
  try {
    const foods = await Food.find({ restaurant: req.params.restaurantId });
    res.json({ success: true, count: foods.length, data: foods });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Add a food item
// @route   POST /api/food
// @access  Private (Restaurant Owner / Admin)
exports.addFood = async (req, res) => {
  try {
    const { restaurantId, categoryId, name, description, price, image } = req.body;

    let resId = restaurantId;
    if (!resId && req.user.role === 'RESTAURANT') {
      const rest = await Restaurant.findOne({ owner: req.user._id });
      if (rest) resId = rest._id;
    }

    if (!resId || !name || price === undefined) {
      return res.status(400).json({ success: false, message: 'Restaurant, name, and price are required' });
    }

    const food = await Food.create({
      restaurant: resId,
      category: categoryId || null,
      name,
      description: description || '',
      price: Number(price),
      image: image || ''
    });

    res.status(201).json({ success: true, data: food });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Update food item
// @route   PUT /api/food/:id
// @access  Private (Restaurant Owner / Admin)
exports.updateFood = async (req, res) => {
  try {
    let food = await Food.findById(req.params.id);
    if (!food) {
      return res.status(404).json({ success: false, message: 'Food item not found' });
    }

    const { name, description, price, image, isAvailable } = req.body;
    if (name) food.name = name;
    if (description !== undefined) food.description = description;
    if (price !== undefined) food.price = Number(price);
    if (image !== undefined) food.image = image;
    if (isAvailable !== undefined) food.isAvailable = isAvailable;

    const updated = await food.save();
    res.json({ success: true, data: updated });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Delete food item
// @route   DELETE /api/food/:id
// @access  Private (Restaurant Owner / Admin)
exports.deleteFood = async (req, res) => {
  try {
    const food = await Food.findByIdAndDelete(req.params.id);
    if (!food) {
      return res.status(404).json({ success: false, message: 'Food item not found' });
    }
    res.json({ success: true, message: 'Food item deleted successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
