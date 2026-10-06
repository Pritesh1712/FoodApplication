const mongoose = require('mongoose');
const dotenv = require('dotenv');
const User = require('./models/User');
const Restaurant = require('./models/Restaurant');
const Category = require('./models/Category');
const Food = require('./models/Food');
const Order = require('./models/Order');
const connectDB = require('./config/db');

dotenv.config();
connectDB();

const importData = async () => {
  try {
    await User.deleteMany();
    await Restaurant.deleteMany();
    await Category.deleteMany();
    await Food.deleteMany();
    await Order.deleteMany();

    console.log('Cleared all existing database records...');

    // Seed ONLY pre-configured System Admin account
    const adminEmail = (process.env.ADMIN_EMAIL || 'admin@gmail.com').trim().toLowerCase();
    const adminPass = process.env.ADMIN_PASSWORD || 'admin@123';

    await User.create({
      name: 'admin',
      email: adminEmail,
      password: adminPass,
      phone: '9999999999',
      role: 'ADMIN'
    });

    console.log('Fresh Database Seeded! Admin account ready: ' + adminEmail);
    process.exit();
  } catch (error) {
    console.error('Seeder Error:', error);
    process.exit(1);
  }
};

importData();
