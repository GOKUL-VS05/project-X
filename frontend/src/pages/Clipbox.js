import { useState } from "react";

export default function Clipbox() {
  const [text, setText] = useState("");
  const [history, setHistory] = useState([]);

  const syncClipboard = () => {
    if (!text.trim()) {
      return;
    }

    setHistory((currentHistory) => [text, ...currentHistory]);
    setText("");
  };

  return (
    <div className="container">
      <section className="panel">
        <p className="eyebrow">Clipboard Sync</p>
        <h2>Universal Clipbox</h2>
        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Paste text, code, or notes to sync across devices"
        />
        <button onClick={syncClipboard}>Sync</button>

        <ul className="clip-history">
          {history.map((item, index) => (
            <li key={`${item}-${index}`}>{item}</li>
          ))}
        </ul>
      </section>
    </div>
  );
}
