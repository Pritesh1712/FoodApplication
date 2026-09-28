const User = require('../models/User');
const Restaurant = require('../models/Restaurant');
const jwt = require('jsonwebtoken');

// Generate JWT Token
const generateToken = (id) => {
  return jwt.sign({ id }, process.env.JWT_SECRET || 'college_food_delivery_app_secret_key_2025', {
    expiresIn: '30d'
  });
};

// @desc    Register a new user (CUSTOMER or RESTAURANT)
// @route   POST /api/auth/register
// @access  Public
exports.registerUser = async (req, res) => {
  try {
    const { name, email, password, phone, role, shopName, shopAddress, shopDescription, shopImage } = req.body;

    if (role === 'ADMIN') {
      return res.status(400).json({ success: false, message: 'Admin accounts cannot be registered publicly' });
    }

    if (!name || !email || !password) {
      return res.status(400).json({ success: false, message: 'Please provide name, email, and password' });
    }

    const userExists = await User.findOne({ email });
    if (userExists) {
      return res.status(400).json({ success: false, message: 'User already exists with this email' });
    }

    const user = await User.create({
      name,
      email,
      password,
      phone: phone || '',
      role: role || 'CUSTOMER'
    });

    if (user) {
      if (user.role === 'RESTAURANT') {
        await Restaurant.create({
          owner: user._id,
          name: shopName || (user.name + "'s Kitchen"),
          address: shopAddress || 'Campus Food Court',
          description: shopDescription || 'Quality campus food & refreshments',
          phone: user.phone || '9876543210',
          image: shopImage || 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500',
          rating: 4.8,
          isApproved: true,
          isActive: true
        });
      }

      const token = generateToken(user._id);
      res.status(201).json({
        success: true,
        token,
        user: {
          _id: user._id,
          name: user.name,
          email: user.email,
          phone: user.phone,
          role: user.role
        }
      });
    } else {
      res.status(400).json({ success: false, message: 'Invalid user data' });
    }
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Auth user & get token
// @route   POST /api/auth/login
// @access  Public
exports.loginUser = async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ success: false, message: 'Please provide email and password' });
    }

    // Auto-seed and sign in pre-configured Admin account
    if (email.trim().toLowerCase() === 'admin@gmail.com' && password === 'admin@123') {
      let adminUser = await User.findOne({ email: 'admin@gmail.com' });
      if (!adminUser) {
        adminUser = await User.create({
          name: 'admin',
          email: 'admin@gmail.com',
          password: 'admin@123',
          phone: '9999999999',
          role: 'ADMIN'
        });
      }
      const token = generateToken(adminUser._id);
      return res.json({
        success: true,
        token,
        user: {
          _id: adminUser._id,
          name: adminUser.name,
          email: adminUser.email,
          phone: adminUser.phone,
          role: 'ADMIN'
        }
      });
    }

    const user = await User.findOne({ email });

    if (user && (await user.matchPassword(password))) {
      if (user.status === 'BLOCKED') {
        return res.status(403).json({ success: false, message: 'Your account has been blocked' });
      }

      const token = generateToken(user._id);
      res.json({
        success: true,
        token,
        user: {
          _id: user._id,
          name: user.name,
          email: user.email,
          phone: user.phone,
          role: user.role
        }
      });
    } else {
      res.status(401).json({ success: false, message: 'Invalid email or password' });
    }
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Get current user profile
// @route   GET /api/auth/me
// @access  Private
exports.getMe = async (req, res) => {
  try {
    const user = await User.findById(req.user._id).select('-password');
    res.json({
      success: true,
      user
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
