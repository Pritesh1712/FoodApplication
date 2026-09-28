const express = require('express');
const router = express.Router();
const {
  getRestaurants,
  getRestaurantById,
  getMyRestaurant,
  createRestaurant
} = require('../controllers/restaurantController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.get('/', getRestaurants);
router.get('/my/restaurant', protect, authorize('RESTAURANT', 'ADMIN'), getMyRestaurant);
router.get('/:id', getRestaurantById);
router.post('/', protect, authorize('RESTAURANT', 'ADMIN'), createRestaurant);

module.exports = router;
