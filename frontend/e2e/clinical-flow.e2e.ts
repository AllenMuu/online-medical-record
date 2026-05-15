import { expect, type Page, test } from '@playwright/test';

const timestamp = Date.now();
const doctor = {
  name: `端到端医生${timestamp}`,
  email: `doctor.e2e.${timestamp}@aura.local`,
  password: 'Doctor123!',
};
const patient = {
  name: `端到端患者${timestamp}`,
  diagnosis: `端到端诊断${timestamp}`,
};

async function login(page: Page, email: string, password: string) {
  await page.goto('/login');
  await page.getByPlaceholder('请输入您的账号').fill(email);
  await page.getByPlaceholder('请输入登录密码').fill(password);
  await page.getByRole('button', { name: '立即登录' }).click();
  await expect(page.getByRole('heading', { name: '仪表盘' })).toBeVisible();
}

test('admin creates doctor, doctor records a patient visit, and history search finds it', async ({ page }) => {
  await login(page, 'admin@aura.local', 'Admin123!');

  await page.getByRole('link', { name: '医生账号' }).click();
  await expect(page.getByRole('heading', { name: '医生账号' })).toBeVisible();
  await page.locator('label').filter({ hasText: '姓名' }).getByRole('textbox').fill(doctor.name);
  await page.locator('label').filter({ hasText: '邮箱' }).getByRole('textbox').fill(doctor.email);
  await page.locator('label').filter({ hasText: '初始密码' }).getByRole('textbox').fill(doctor.password);
  await page.getByRole('button', { name: '创建医生账号' }).click();
  await expect(page.getByText(doctor.email)).toBeVisible();

  await page.getByRole('button', { name: '退出登录' }).click();
  await login(page, doctor.email, doctor.password);

  await page.getByRole('link', { name: '患者管理' }).click();
  await page.getByRole('button', { name: '新增患者' }).click();
  const dialog = page.locator('form').filter({ hasText: '新增患者档案' });
  await dialog.locator('label').filter({ hasText: '姓名' }).getByRole('textbox').fill(patient.name);
  await dialog.locator('label').filter({ hasText: '年龄' }).getByRole('spinbutton').fill('42');
  await dialog.locator('label').filter({ hasText: '所属队伍' }).getByRole('textbox').fill('端到端测试组');
  await dialog.locator('label').filter({ hasText: '临床摘要' }).getByRole('textbox').fill('E2E 自动化新增患者');
  await page.getByRole('button', { name: '保存患者' }).click();
  await expect(page.getByText(patient.name)).toBeVisible();

  await page.getByRole('button', { name: '快速录入病历' }).click();
  await expect(page.getByRole('heading', { name: '录入新病历' })).toBeVisible();
  await page.locator('label').filter({ hasText: '姓名' }).getByRole('combobox').selectOption({ label: patient.name });
  await page.locator('label').filter({ hasText: '身体状况/主诉' }).getByRole('textbox').fill('主诉来自 E2E 测试');
  await page.locator('label').filter({ hasText: '查体' }).getByRole('textbox').fill('生命体征平稳');
  await page.locator('label').filter({ hasText: '诊断' }).getByRole('textbox').fill(patient.diagnosis);
  await page.locator('label').filter({ hasText: '处置' }).getByRole('textbox').fill('完善检查并随访');
  await page.getByRole('button', { name: '提交档案' }).click();

  await expect(page.getByRole('heading', { name: '历史病历列表' })).toBeVisible();
  await expect(page.getByText(patient.diagnosis)).toBeVisible();
  await page.locator('label').filter({ hasText: '姓名搜索' }).getByRole('textbox').fill(patient.name);
  await page.getByRole('button', { name: '应用筛选' }).click();
  await expect(page.getByText(patient.name)).toBeVisible();
  await expect(page.getByText(patient.diagnosis)).toBeVisible();
});
