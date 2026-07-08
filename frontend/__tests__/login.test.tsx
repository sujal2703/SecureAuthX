import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { LoginForm } from "@/components/forms/login-form";
import { AuthProvider } from "@/contexts/auth-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useAuth } from "@/hooks/use-auth";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));

jest.mock("@/hooks/use-auth", () => ({
  useAuth: jest.fn(),
}));

const mockLogin = jest.fn();
const mockPush = jest.fn();

function renderWithProviders(ui: React.ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>{ui}</AuthProvider>
    </QueryClientProvider>,
  );
}

describe("LoginForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue({ push: mockPush });
    (useAuth as jest.Mock).mockReturnValue({ login: mockLogin });
  });

  it("renders the login form with all fields", () => {
    renderWithProviders(<LoginForm />);
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /sign in/i }),
    ).toBeInTheDocument();
  });

  it("shows validation errors for empty fields", async () => {
    renderWithProviders(<LoginForm />);
    const submitButton = screen.getByRole("button", { name: /sign in/i });
    fireEvent.click(submitButton);
    await waitFor(() => {
      expect(screen.getByText(/invalid email/i)).toBeInTheDocument();
      expect(screen.getByText(/password is required/i)).toBeInTheDocument();
    });
  });

  it("shows validation error for invalid email", async () => {
    renderWithProviders(<LoginForm />);
    const emailInput = screen.getByLabelText(/email/i) as HTMLInputElement;
    const passwordInput = screen.getByLabelText(/password/i) as HTMLInputElement;
    fireEvent.change(emailInput, { target: { value: "invalid-email" } });
    fireEvent.change(passwordInput, { target: { value: "password123" } });
    const form = emailInput.closest("form")!;
    fireEvent.submit(form);
    await waitFor(() => {
      expect(screen.getByText(/invalid email/i)).toBeInTheDocument();
    });
  });

  it("calls login on valid submission", async () => {
    mockLogin.mockResolvedValueOnce(undefined);
    renderWithProviders(<LoginForm />);
    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: "test@example.com" },
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: "password123" },
    });
    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));
    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith(
        "test@example.com",
        "password123",
      );
      expect(mockPush).toHaveBeenCalledWith("/dashboard");
    });
  });

  it("displays error message on login failure", async () => {
    mockLogin.mockRejectedValueOnce({
      response: { data: { message: "Invalid credentials" } },
    });
    renderWithProviders(<LoginForm />);
    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: "test@example.com" },
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: "wrong" },
    });
    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));
    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
    });
  });

  it("disables submit button while loading", async () => {
    mockLogin.mockImplementationOnce(
      () => new Promise((resolve) => setTimeout(resolve, 1000)),
    );
    renderWithProviders(<LoginForm />);
    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: "test@example.com" },
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: "password123" },
    });
    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));
    await waitFor(() => {
      expect(
        screen.getByRole("button", { name: /signing in/i }),
      ).toBeDisabled();
    });
  });
});
