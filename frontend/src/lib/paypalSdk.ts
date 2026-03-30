export const PAYPAL_SDK_ERRORS = {
  SDK_LOAD_FAILED: 'SDK_LOAD_FAILED',
  SDK_CLIENT_ID_MISSING: 'SDK_CLIENT_ID_MISSING',
} as const

export type PayPalSdkErrorCode = (typeof PAYPAL_SDK_ERRORS)[keyof typeof PAYPAL_SDK_ERRORS]

export class PayPalSdkError extends Error {
  code: PayPalSdkErrorCode

  constructor(code: PayPalSdkErrorCode, message: string) {
    super(message)
    this.name = 'PayPalSdkError'
    this.code = code
  }
}

export const buildPayPalSdkUrl = (clientId: string, currency = 'USD') => {
  const trimmedId = clientId.trim()
  if (!trimmedId) {
    throw new PayPalSdkError(PAYPAL_SDK_ERRORS.SDK_CLIENT_ID_MISSING, 'Missing PayPal client id')
  }

  const params = new URLSearchParams({
    'client-id': trimmedId,
    currency,
  })

  return `https://www.paypal.com/sdk/js?${params.toString()}`
}

const existingSdkScript = () => document.querySelector<HTMLScriptElement>('script[data-paypal-sdk="true"]')

export const loadPayPalSdk = async (clientId: string, currency = 'USD') => {
  if (window.paypal?.Buttons) return

  const currentScript = existingSdkScript()
  if (currentScript) {
    await new Promise<void>((resolve, reject) => {
      if (window.paypal?.Buttons) {
        resolve()
        return
      }

      currentScript.addEventListener('load', () => resolve(), { once: true })
      currentScript.addEventListener(
        'error',
        () => reject(new PayPalSdkError(PAYPAL_SDK_ERRORS.SDK_LOAD_FAILED, 'PayPal SDK failed to load')),
        { once: true }
      )
    })
    return
  }

  const src = buildPayPalSdkUrl(clientId, currency)
  await new Promise<void>((resolve, reject) => {
    const script = document.createElement('script')
    script.src = src
    script.async = true
    script.dataset.paypalSdk = 'true'

    script.onload = () => resolve()
    script.onerror = () => reject(new PayPalSdkError(PAYPAL_SDK_ERRORS.SDK_LOAD_FAILED, 'PayPal SDK failed to load'))

    document.head.appendChild(script)
  })
}

declare global {
  interface Window {
    paypal?: {
      Buttons: (options: {
        createOrder: () => Promise<string>
        onApprove: (data: { orderID?: string }) => Promise<void>
        onError: (err: unknown) => void
      }) => {
        render: (selector: string) => Promise<void>
        close: () => void
      }
    }
  }
}
