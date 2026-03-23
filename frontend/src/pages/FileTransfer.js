import { useState } from "react";

export default function FileTransfer() {
  const [fileName, setFileName] = useState("");

  return (
    <div className="container">
      <section className="panel">
        <p className="eyebrow">Transfer Queue</p>
        <h2>File Transfer</h2>
        <label className="file-input">
          <span>{fileName || "Choose a file"}</span>
          <input
            type="file"
            onChange={(e) => setFileName(e.target.files?.[0]?.name || "")}
          />
        </label>
        <p className="muted-text">Files will be transferred via WebRTC or cloud relay.</p>
      </section>
    </div>
  );
}
