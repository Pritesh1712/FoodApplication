const express = require('express');
const router = express.Router();
const {
  createOrder,
  getMyOrders,
  getRestaurantOrders,
  getDeliveryOrders,
  updateOrderStatus
} = require('../controllers/orderController');
const { protect, authorize } = require('../middleware/authMiddleware');

router.post('/', protect, authorize('CUSTOMER'), createOrder);
router.get('/my', protect, getMyOrders);
router.get('/restaurant/:restaurantId', protect, authorize('RESTAURANT', 'ADMIN'), getRestaurantOrders);
router.get('/delivery', protect, authorize('DELIVERY', 'ADMIN'), getDeliveryOrders);
router.put('/:id/status', protect, updateOrderStatus);

module.exports = router;
