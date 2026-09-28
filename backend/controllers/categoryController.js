const mongoose = require('mongoose');
const Category = require('../models/Category');
const Restaurant = require('../models/Restaurant');

const DEFAULT_CATEGORIES = [
  { name: 'Starters', image: 'https://images.unsplash.com/photo-1541544741938-0af808871cc0?w=300', restaurant: null },
  { name: 'Main Course', image: 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=300', restaurant: null },
  { name: 'Sweets & Desserts', image: 'https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=300', restaurant: null }
];

exports.getCategories = async (req, res) => {
  try {
    const { restaurantId } = req.query;

    let globalCats = await Category.find({ restaurant: null });
    if (globalCats.length === 0) {
      globalCats = await Category.insertMany(DEFAULT_CATEGORIES);
    }

    let result = [...globalCats];

    if (restaurantId && mongoose.Types.ObjectId.isValid(restaurantId)) {
      const shopCats = await Category.find({ restaurant: restaurantId });
      result = [...result, ...shopCats];
    } else if (req.user && req.user.role === 'RESTAURANT') {
      const rest = await Restaurant.findOne({ owner: req.user._id });
      if (rest) {
        const shopCats = await Category.find({ restaurant: rest._id });
        result = [...result, ...shopCats];
      }
    }

    res.json({ success: true, count: result.length, data: result });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.createCategory = async (req, res) => {
  try {
    const { name, image, restaurantId } = req.body;
    if (!name) {
      return res.status(400).json({ success: false, message: 'Category name is required' });
    }

    let targetRestId = restaurantId;
    if ((!targetRestId || !mongoose.Types.ObjectId.isValid(targetRestId)) && req.user) {
      const rest = await Restaurant.findOne({ owner: req.user._id });
      if (rest) targetRestId = rest._id;
    }

    const defaultImg = 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=300';
    const category = await Category.create({
      name,
      image: (image && image.trim().length > 0) ? image : defaultImg,
      restaurant: (targetRestId && mongoose.Types.ObjectId.isValid(targetRestId)) ? targetRestId : null
    });

    res.status(201).json({ success: true, data: category });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
