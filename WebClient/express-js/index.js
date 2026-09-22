const express = require('express');
const cors = require('cors');
const { port } = require('./config/env');
const apiRoutes = require('./routes/api');

const app = express();

app.use(cors());
app.use(express.json());

app.use('/api', apiRoutes);

app.get('/health', (req, res) => res.json({ status: 'ok' }));

app.listen(port, () => {
  console.log(`API Express escuchando en http://localhost:${port}`);
});
