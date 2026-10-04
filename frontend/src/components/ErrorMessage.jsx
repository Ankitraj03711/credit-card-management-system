export default function ErrorMessage({ message, onRetry }) {
  return <div className="error-message"><strong>Something went wrong.</strong> {message}{onRetry && <button onClick={onRetry} className="text-button">Retry</button>}</div>
}
