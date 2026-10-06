const User = require('../models/User');
const Restaurant = require('../models/Restaurant');
const jwt = require('jsonwebtoken');

// Email & Phone Validation Regexes
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_REGEX = /^[0-9]{10}$/;

// Generate JWT Token
const generateToken = (id) => {
  const secret = process.env.JWT_SECRET;
  if (!secret) {
    throw new Error('JWT_SECRET is missing in environment variables');
  }
  return jwt.sign({ id }, secret, {
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

    if (!name || !email || !password || !phone) {
      return res.status(400).json({ success: false, message: 'Please provide name, email, password, and phone number' });
    }

    const cleanEmail = email.trim().toLowerCase();
    const cleanPhone = phone.trim();

    if (!EMAIL_REGEX.test(cleanEmail)) {
      return res.status(400).json({ success: false, message: 'Invalid email address format (e.g. abc@gmail.com)' });
    }

    if (!PHONE_REGEX.test(cleanPhone)) {
      return res.status(400).json({ success: false, message: 'Mobile number must contain exactly 10 numeric digits' });
    }

    const userExists = await User.findOne({ email: cleanEmail });
    if (userExists) {
      return res.status(400).json({ success: false, message: 'User already exists with this email address' });
    }

    const user = await User.create({
      name: name.trim(),
      email: cleanEmail,
      password,
      phone: cleanPhone,
      role: role || 'CUSTOMER'
    });

    if (user) {
      if (user.role === 'RESTAURANT') {
        await Restaurant.create({
          owner: user._id,
          name: shopName || (user.name + "'s Kitchen"),
          address: shopAddress || 'Campus Food Court',
          description: shopDescription || 'Quality campus food & refreshments',
          phone: user.phone,
          image: shopImage || 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500',
          rating: 4.8,
          isApproved: false,
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

// @desc    Auth user & get token (With Role Enforcement)
// @route   POST /api/auth/login
// @access  Public
exports.loginUser = async (req, res) => {
  try {
    const { email, password, role } = req.body;

    if (!email || !password) {
      return res.status(400).json({ success: false, message: 'Please provide email and password' });
    }

    const cleanEmail = email.trim().toLowerCase();
    const adminEmail = (process.env.ADMIN_EMAIL || 'admin@gmail.com').trim().toLowerCase();
    const adminPass = process.env.ADMIN_PASSWORD || 'admin@123';

    // Auto-seed and sign in pre-configured Admin account
    if (cleanEmail === adminEmail && password === adminPass) {
      if (role && role !== 'ADMIN') {
        return res.status(403).json({ success: false, message: 'Role mismatch! System Admin can only sign in under the ADMIN role.' });
      }

      let adminUser = await User.findOne({ email: adminEmail });
      if (!adminUser) {
        adminUser = await User.create({
          name: 'admin',
          email: adminEmail,
          password: adminPass,
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

    const user = await User.findOne({ email: cleanEmail });

    if (user && (await user.matchPassword(password))) {
      if (user.status === 'BLOCKED') {
        return res.status(403).json({ success: false, message: 'Your account has been blocked' });
      }

      // Enforce Role Lock: Account registered role must match requested role
      if (role && user.role !== role) {
        return res.status(403).json({
          success: false,
          message: `Role mismatch! This account is registered as a ${user.role}. Please select the ${user.role} role to sign in.`
        });
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
