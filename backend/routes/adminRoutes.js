const express = require('express');
const router = express.Router();
const {
  getAllUsers,
  deleteUser,
  deleteRestaurant,
  toggleRestaurantStatus,
  getAdminStats
} = require('../controllers/adminController');
const { protect, authorize } = require('../middleware/authMiddleware');

// Enforce authentication AND Admin authorization for all admin routes
router.use(protect, authorize('ADMIN'));

router.get('/users', getAllUsers);
router.delete('/users/:id', deleteUser);
router.delete('/restaurants/:id', deleteRestaurant);
router.put('/restaurants/:id/approve', toggleRestaurantStatus);
router.get('/stats', getAdminStats);

module.exports = router;
