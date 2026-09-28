const mongoose = require('mongoose');
const dotenv = require('dotenv');
const User = require('./models/User');
const Restaurant = require('./models/Restaurant');
const Category = require('./models/Category');
const Food = require('./models/Food');
const Coupon = require('./models/Coupon');
const connectDB = require('./config/db');

dotenv.config();
connectDB();

const importData = async () => {
  try {
    await User.deleteMany();
    await Restaurant.deleteMany();
    await Category.deleteMany();
    await Food.deleteMany();
    await Coupon.deleteMany();

    console.log('Cleared existing database records...');

    // 1. Create System Users
    const admin = await User.create({
      name: 'Campus Admin',
      email: 'admin@campus.edu',
      password: 'password123',
      phone: '9999999999',
      role: 'ADMIN'
    });

    const restOwner1 = await User.create({
      name: 'Chef Marco',
      email: 'pizza@campus.edu',
      password: 'password123',
      phone: '9876543210',
      role: 'RESTAURANT'
    });

    const restOwner2 = await User.create({
      name: 'Chef Raj',
      email: 'spice@campus.edu',
      password: 'password123',
      phone: '9876543211',
      role: 'RESTAURANT'
    });

    const delivery1 = await User.create({
      name: 'Alex Rider',
      email: 'delivery@campus.edu',
      password: 'password123',
      phone: '9123456789',
      role: 'DELIVERY'
    });

    const customer1 = await User.create({
      name: 'John Student',
      email: 'student@campus.edu',
      password: 'password123',
      phone: '9888877776',
      role: 'CUSTOMER'
    });

    // 2. Create Categories
    const catPizza = await Category.create({ name: 'Pizza' });
    const catBurgers = await Category.create({ name: 'Burgers' });
    const catIndian = await Category.create({ name: 'Indian' });
    const catChinese = await Category.create({ name: 'Chinese' });
    const catRolls = await Category.create({ name: 'Rolls' });
    const catBeverages = await Category.create({ name: 'Coffee & Shakes' });

    // 3. Create Restaurants
    const r1 = await Restaurant.create({
      owner: restOwner1._id,
      name: 'Campus Pizza Hub',
      description: 'Artisanal fresh-baked pizzas, garlic bread & dips',
      address: 'Student Center, Floor 1',
      phone: '9876543210',
      image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500',
      rating: 4.8
    });

    const r2 = await Restaurant.create({
      owner: restOwner2._id,
      name: 'Spice Junction',
      description: 'Authentic North Indian thalis, paneer butter masala & biryani',
      address: 'Hostel Block C Market',
      phone: '9876543211',
      image: 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=500',
      rating: 4.6
    });

    const r3 = await Restaurant.create({
      owner: restOwner1._id,
      name: 'The Burger Club',
      description: 'Gourmet grilled burgers, crispy fries & thick milkshakes',
      address: 'North Gate Food Street',
      phone: '9876543212',
      image: 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500',
      rating: 4.7
    });

    // 4. Create Food Items
    await Food.create([
      { restaurant: r1._id, category: catPizza._id, name: 'Margherita Pizza', description: 'Classic mozzarella cheese and tomato sauce', price: 249, image: 'https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?w=500' },
      { restaurant: r1._id, category: catPizza._id, name: 'Farmhouse Pizza', description: 'Capsicum, onion, mushroom, corn and cheese', price: 349, image: 'https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?w=500' },
      { restaurant: r1._id, category: catPizza._id, name: 'Cheesy Garlic Bread', description: 'Crispy garlic bread loaded with mozzarella', price: 149, image: 'https://images.unsplash.com/photo-1619895092538-128341789043?w=500' },
      { restaurant: r2._id, category: catIndian._id, name: 'Paneer Butter Masala', description: 'Rich creamy paneer gravy served fresh', price: 220, image: 'https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=500' },
      { restaurant: r2._id, category: catIndian._id, name: 'Hyderabadi Veg Biryani', description: 'Fragrant basmati rice cooked with spices', price: 190, image: 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500' },
      { restaurant: r2._id, category: catIndian._id, name: 'Butter Naan', description: 'Soft tandoori naan brushed with butter', price: 40 },
      { restaurant: r3._id, category: catBurgers._id, name: 'Classic Veg Burger', description: 'Crispy patty with lettuce and mayo', price: 119, image: 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=500' },
      { restaurant: r3._id, category: catBeverages._id, name: 'Belgian Chocolate Shake', description: 'Rich chocolate milkshake with ice cream', price: 129, image: 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=500' }
    ]);

    // 5. Create Coupons
    await Coupon.create({
      code: 'CAMPUS50',
      discountAmount: 50,
      minOrderValue: 200
    });

    console.log('Sample Data Seeded Successfully!');
    process.exit();
  } catch (error) {
    console.error('Seeder Error:', error);
    process.exit(1);
  }
};

importData();
