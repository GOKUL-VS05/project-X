require("dotenv").config();

const express = require("express");
const http = require("http");
const cors = require("cors");
const { Server } = require("socket.io");

const PORT = process.env.PORT || 5000;
const CLIENT_URL = process.env.CLIENT_URL || "http://localhost:3000";

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: CLIENT_URL,
    methods: ["GET", "POST"],
  },
});

app.use(
  cors({
    origin: CLIENT_URL,
  })
);
app.use(express.json());

app.get("/", (_req, res) => {
  res.json({
    name: "Cross Device Backend",
    status: "ok",
    signaling: true,
  });
});

const clients = {};

io.on("connection", (socket) => {
  console.log("Connected:", socket.id);

  socket.on("register", (id) => {
    if (id) {
      clients[id] = socket.id;
    }
  });

  socket.on("offer", ({ to, offer }) => {
    if (to && clients[to]) {
      io.to(clients[to]).emit("offer", offer);
    }
  });

  socket.on("answer", ({ to, answer }) => {
    if (to && clients[to]) {
      io.to(clients[to]).emit("answer", answer);
    }
  });

  socket.on("candidate", ({ to, candidate }) => {
    if (to && clients[to]) {
      io.to(clients[to]).emit("candidate", candidate);
    }
  });

  socket.on("disconnect", () => {
    Object.keys(clients).forEach((clientId) => {
      if (clients[clientId] === socket.id) {
        delete clients[clientId];
      }
    });
  });
});

server.listen(PORT, () => {
  console.log(`Cross Device backend running on http://localhost:${PORT}`);
});
