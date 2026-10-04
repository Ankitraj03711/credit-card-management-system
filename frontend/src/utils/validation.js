export function validatePositiveAmount(value) {
  const amount = Number(value)
  return Number.isFinite(amount) && amount > 0 ? '' : 'Amount must be greater than zero.'
}
