const express = require('express');
const router = express.Router();
const {
  getFoodsByRestaurant,
  addFood,
  updateFood,
  deleteFood
} = require('../controllers/foodController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.get('/restaurant/:restaurantId', getFoodsByRestaurant);
router.post('/', protect, authorize('RESTAURANT', 'ADMIN'), addFood);
router.put('/:id', protect, authorize('RESTAURANT', 'ADMIN'), updateFood);
router.delete('/:id', protect, authorize('RESTAURANT', 'ADMIN'), deleteFood);

module.exports = router;
