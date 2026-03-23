export default function DeviceCard({ name, type, status }) {
  return (
    <article className="device-card">
      <div className="device-card__badge">{type}</div>
      <h3>{name}</h3>
      <p>{status}</p>
    </article>
  );
}
