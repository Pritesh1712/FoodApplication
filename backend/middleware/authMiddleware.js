const jwt = require('jsonwebtoken');
const User = require('../models/User');

const protect = async (req, res, next) => {
  let token;

  if (
    req.headers.authorization &&
    req.headers.authorization.startsWith('Bearer')
  ) {
    try {
      token = req.headers.authorization.split(' ')[1];
      const secret = process.env.JWT_SECRET;
      if (!secret) {
        return res.status(500).json({ success: false, message: 'Server error: JWT_SECRET is not configured' });
      }

      const decoded = jwt.verify(token, secret);

      req.user = await User.findById(decoded.id).select('-password');

      if (!req.user) {
        return res.status(401).json({ success: false, message: 'User not found or token invalid' });
      }

      if (req.user.status === 'BLOCKED') {
        return res.status(403).json({ success: false, message: 'Your account has been blocked' });
      }

      next();
    } catch (error) {
      console.error('JWT Auth Error:', error.message);
      return res.status(401).json({ success: false, message: 'Not authorized, token failed' });
    }
  }

  if (!token) {
    return res.status(401).json({ success: false, message: 'Not authorized, no token provided' });
  }
};

const authorize = (...roles) => {
  return (req, res, next) => {
    if (!req.user || !roles.includes(req.user.role)) {
      return res.status(403).json({
        success: false,
        message: `User role '${req.user ? req.user.role : 'GUEST'}' is not authorized to access this route`
      });
    }
    next();
  };
};

module.exports = { protect, authorize };
