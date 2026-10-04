const colors = { ACTIVE: 'status-active', BLOCKED: 'status-blocked', CLOSED: 'status-closed', INACTIVE: 'status-closed', PENDING: 'status-pending', COMPLETED: 'status-active', FAILED: 'status-blocked' }
export default function StatusBadge({ value }) {
  if (!value) return <span className="muted">—</span>
  return <span className={`status-badge ${colors[value] || 'status-default'}`}>{value.replaceAll('_', ' ')}</span>
}
