import { QRCodeSVG } from "qrcode.react";
import DeviceCard from "../components/DeviceCard";

const devices = [
  { name: "Pixel 9 Pro", type: "Mobile", status: "Ready for pairing" },
  { name: "Workstation", type: "Desktop", status: "Primary streaming host" },
  { name: "Tablet Air", type: "Tablet", status: "Clipboard sync enabled" },
];

export default function Dashboard() {
  return (
    <div className="container">
      <section className="hero-card">
        <div>
          <p className="eyebrow">Device Pairing</p>
          <h2>Connect every screen in one session</h2>
          <p className="muted-text">Scan this QR code from the mobile app to pair instantly.</p>
        </div>
        <div className="qr-card">
          <QRCodeSVG value="pair-session-12345" size={180} />
          <p>Session: pair-session-12345</p>
        </div>
      </section>

      <section className="device-grid">
        {devices.map((device) => (
          <DeviceCard key={device.name} {...device} />
        ))}
      </section>
    </div>
  );
}
