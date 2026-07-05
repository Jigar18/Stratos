import { useEffect, useMemo, useState } from "react";

const githubAppSlug = import.meta.env.VITE_GITHUB_APP_SLUG;
const githubAppInstallationUrl =
  import.meta.env.VITE_GITHUB_APP_INSTALL_URL ||
  (githubAppSlug ? `https://github.com/apps/${githubAppSlug}/installations/new` : "");

function LoginPage() {
  return (
    <main className="auth-page">
      <section className="auth-panel" aria-label="GitHub sign in">
        <p className="eyebrow">Stratos</p>
        <h1>Connect GitHub</h1>
        <a
          className="github-button"
          href={githubAppInstallationUrl}
          aria-disabled={!githubAppInstallationUrl}
          onClick={(event) => {
            if (!githubAppInstallationUrl) {
              event.preventDefault();
            }
          }}
        >
          <span aria-hidden="true" className="github-mark">
            <svg viewBox="0 0 24 24" role="img">
              <path d="M12 2C6.48 2 2 6.58 2 12.26c0 4.53 2.87 8.37 6.84 9.73.5.09.68-.22.68-.49 0-.24-.01-1.05-.01-1.9-2.78.62-3.37-1.22-3.37-1.22-.45-1.19-1.11-1.5-1.11-1.5-.91-.64.07-.63.07-.63 1 .07 1.53 1.06 1.53 1.06.89 1.56 2.34 1.11 2.91.85.09-.66.35-1.11.63-1.36-2.22-.26-4.56-1.14-4.56-5.07 0-1.12.39-2.03 1.03-2.75-.1-.26-.45-1.31.1-2.71 0 0 .84-.28 2.75 1.05A9.35 9.35 0 0 1 12 6.98c.85 0 1.7.12 2.5.34 1.9-1.33 2.74-1.05 2.74-1.05.55 1.4.2 2.45.1 2.71.64.72 1.03 1.63 1.03 2.75 0 3.94-2.34 4.81-4.57 5.06.36.32.68.94.68 1.9 0 1.37-.01 2.47-.01 2.81 0 .27.18.59.69.49A10.07 10.07 0 0 0 22 12.26C22 6.58 17.52 2 12 2Z" />
            </svg>
          </span>
          Sign in with GitHub
        </a>
      </section>
    </main>
  );
}

function DashboardPage() {
  return (
    <main className="dashboard-page">
      <section className="dashboard-shell">
        <h1>GitHub app installation is completed successfully</h1>
      </section>
    </main>
  );
}

function GitHubInstallationSuccessRedirect() {
  useEffect(() => {
    window.history.replaceState(null, "", "/dashboard");
    window.dispatchEvent(new PopStateEvent("popstate"));
  }, []);

  return (
    <main className="loading-page">
      <p>Opening dashboard...</p>
    </main>
  );
}

function usePathname() {
  const [pathname, setPathname] = useState(window.location.pathname);

  useEffect(() => {
    const updatePathname = () => setPathname(window.location.pathname);

    window.addEventListener("popstate", updatePathname);
    return () => window.removeEventListener("popstate", updatePathname);
  }, []);

  return pathname;
}

export default function App() {
  const pathname = usePathname();

  return useMemo(() => {
    if (pathname === "/dashboard") {
      return <DashboardPage />;
    }

    if (pathname === "/github/installation/success") {
      return <GitHubInstallationSuccessRedirect />;
    }

    return <LoginPage />;
  }, [pathname]);
}
