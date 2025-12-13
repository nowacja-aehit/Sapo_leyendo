import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest';
import App from './App';
import { login } from './services/api';

vi.mock('./services/api', () => ({
  login: vi.fn(() => Promise.resolve({ token: 'mock-token' })),
  fetchInventory: vi.fn(() => Promise.resolve([])),
  fetchOrders: vi.fn(() => Promise.resolve([])),
  fetchShipments: vi.fn(() => Promise.resolve([])),
}));

describe('App', () => {
  const mockedLogin = vi.mocked(login);

  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  it('renders marketing view by default', () => {
    render(<App />);

    const ctas = screen.getAllByText('Rozpocznij');
    expect(ctas.length).toBeGreaterThan(0);
    expect(screen.queryByText('Witaj ponownie')).toBeNull();
  });

  it('shows login page after clicking CTA', async () => {
    render(<App />);
    const user = userEvent.setup();

    await user.click(screen.getAllByText('Rozpocznij')[0]);

    expect(await screen.findByText('Witaj ponownie')).toBeInTheDocument();
    expect(screen.getByLabelText(/Adres e-mail/i)).toBeInTheDocument();
  });

  it('logs in, persists state, and shows dashboard', async () => {
    render(<App />);
    const user = userEvent.setup();

    await user.click(screen.getAllByText('Rozpocznij')[0]);
    await user.type(screen.getByLabelText(/Adres e-mail/i), 'user@example.com');
    await user.type(screen.getByLabelText(/Hasło/i), 'supersecret');
    await user.click(screen.getByLabelText(/Zapamiętaj mnie/i));

    await user.click(screen.getByRole('button', { name: /Zaloguj się/i }));

    expect(mockedLogin).toHaveBeenCalledWith('user@example.com', 'supersecret');
    expect(await screen.findByText(/Wyloguj/i)).toBeInTheDocument();
    expect(localStorage.getItem('viz_isLoggedIn')).toBe('true');
    expect(localStorage.getItem('rememberedEmail')).toBe('user@example.com');
  });
});
