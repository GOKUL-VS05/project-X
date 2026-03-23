import { useEffect, useRef, useState } from "react";
import { io } from "socket.io-client";

export default function Mirroring() {
  const videoRef = useRef(null);
  const [error, setError] = useState("");
  const [status, setStatus] = useState("Waiting for Android stream...");

  useEffect(() => {
    const socket = io(process.env.REACT_APP_SERVER_URL || "http://localhost:5000");
    const pc = new RTCPeerConnection({
      iceServers: [{ urls: "stun:stun.l.google.com:19302" }],
    });

    pc.ontrack = (event) => {
      if (videoRef.current) {
        videoRef.current.srcObject = event.streams[0];
      }
      setStatus("Receiving Android stream.");
    };

    pc.onicecandidate = (event) => {
      if (event.candidate) {
        socket.emit("candidate", {
          to: "mobile-user",
          candidate: event.candidate.toJSON(),
        });
      }
    };

    socket.emit("register", "desktop-user");

    socket.on("offer", async (offer) => {
      try {
        setError("");
        setStatus("Offer received. Creating answer...");

        await pc.setRemoteDescription(
          new RTCSessionDescription({
            type: "offer",
            sdp: offer,
          })
        );

        const answer = await pc.createAnswer();
        await pc.setLocalDescription(answer);

        socket.emit("answer", {
          to: "mobile-user",
          answer: answer.sdp,
        });

        setStatus("Answer sent to Android.");
      } catch (incomingError) {
        setError(incomingError.message || "Failed to process the Android offer.");
      }
    });

    socket.on("candidate", async (candidate) => {
      try {
        await pc.addIceCandidate(new RTCIceCandidate(candidate));
      } catch (candidateError) {
        setError(candidateError.message || "Failed to add ICE candidate.");
      }
    });

    socket.on("connect_error", () => {
      setError("Could not connect to the signaling server.");
    });

    return () => {
      socket.disconnect();
      pc.close();
    };
  }, []);

  return (
    <div className="container">
      <section className="panel">
        <p className="eyebrow">WebRTC Demo</p>
        <h2>Screen Mirroring</h2>
        <p className="muted-text">
          Keep this page open while the Android app starts capture and sends its stream.
        </p>
        <video ref={videoRef} autoPlay playsInline className="mirror-video" />
        <p className="muted-text">{status}</p>
        {error ? <p className="error-text">{error}</p> : null}
      </section>
    </div>
  );
}
