import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar";
import Clipbox from "./pages/Clipbox";
import Dashboard from "./pages/Dashboard";
import FileTransfer from "./pages/FileTransfer";
import Login from "./pages/Login";
import Mirroring from "./pages/Mirroring";

function App() {
  return (
    <BrowserRouter>
      <div className="app-shell">
        <Navbar />
        <main className="page-shell">
          <Routes>
            <Route path="/" element={<Login />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/mirror" element={<Mirroring />} />
            <Route path="/clipbox" element={<Clipbox />} />
            <Route path="/files" element={<FileTransfer />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
