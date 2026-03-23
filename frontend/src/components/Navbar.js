import { Link, useLocation } from "react-router-dom";

const links = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/mirror", label: "Mirroring" },
  { to: "/clipbox", label: "Clipbox" },
  { to: "/files", label: "File Transfer" },
];

export default function Navbar() {
  const location = useLocation();
  const isLoginPage = location.pathname === "/";

  if (isLoginPage) {
    return null;
  }

  return (
    <nav className="nav">
      <div className="nav__brand">Cross Device Ecosystem</div>
      <div className="nav__links">
        {links.map((link) => (
          <Link
            key={link.to}
            to={link.to}
            className={
              location.pathname === link.to ? "nav__link nav__link--active" : "nav__link"
            }
          >
            {link.label}
          </Link>
        ))}
      </div>
    </nav>
  );
}
