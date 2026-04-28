import { chromium } from 'playwright';

const url = process.env.LOGIN_URL || 'http://127.0.0.1:5766/web-budget/auth/login';
const out = process.env.OUT || '/workspace/login-page-screenshot.png';

const browser = await chromium.launch();
const page = await browser.newPage({ viewport: { width: 1280, height: 800 } });
page.on('console', (msg) => console.log('browser:', msg.type(), msg.text()));
page.on('pageerror', (e) => console.log('pageerror:', e.message));
page.on('requestfailed', (req) =>
  console.log('requestfailed:', req.url(), req.failure()?.errorText),
);
page.on('response', (res) => {
  if (res.status() >= 400)
    console.log('response', res.status(), res.url());
});
await page.goto(url, { waitUntil: 'networkidle', timeout: 120000 });
await page.waitForTimeout(5000);
const inputs = await page.locator('input').count();
console.log('input count', inputs);
await page.screenshot({ path: out, fullPage: true });
await browser.close();
console.log('saved', out);
