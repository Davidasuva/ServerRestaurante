require('dotenv').config();

module.exports = {
  port: process.env.PORT || 3000,
  bridgeUrl: process.env.BRIDGE_URL || 'http://localhost:8081',
};
