import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { RegisterForm } from "@/components/forms/register-form";
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

const mockRegister = jest.fn();
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

describe("RegisterForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue({ push: mockPush });
    (useAuth as jest.Mock).mockReturnValue({ register: mockRegister });
  });

  it("renders the registration form with all fields", () => {
    renderWithProviders(<RegisterForm />);
    expect(screen.getByLabelText(/first name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/last name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^password$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/confirm password/i)).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /create account/i }),
    ).toBeInTheDocument();
  });

  it("shows validation errors for empty fields", async () => {
    renderWithProviders(<RegisterForm />);
    fireEvent.click(
      screen.getByRole("button", { name: /create account/i }),
    );
    await waitFor(() => {
      expect(screen.getByText(/first name is required/i)).toBeInTheDocument();
      expect(screen.getByText(/last name is required/i)).toBeInTheDocument();
      expect(screen.getByText(/invalid email/i)).toBeInTheDocument();
      expect(
        screen.getByText(/password must be at least 8 characters/i),
      ).toBeInTheDocument();
    });
  });

  it("shows error when passwords do not match", async () => {
    renderWithProviders(<RegisterForm />);
    fireEvent.change(screen.getByLabelText(/first name/i), {
      target: { value: "John" },
    });
    fireEvent.change(screen.getByLabelText(/last name/i), {
      target: { value: "Doe" },
    });
    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: "john@example.com" },
    });
    fireEvent.change(screen.getByLabelText(/^password$/i), {
      target: { value: "StrongP@ss1" },
    });
    fireEvent.change(screen.getByLabelText(/confirm password/i), {
      target: { value: "DifferentP@ss1" },
    });
    fireEvent.click(
      screen.getByRole("button", { name: /create account/i }),
    );
    await waitFor(() => {
      expect(screen.getByText(/passwords do not match/i)).toBeInTheDocument();
    });
  });

  it("calls register on valid submission", async () => {
    mockRegister.mockResolvedValueOnce(undefined);
    renderWithProviders(<RegisterForm />);
    fireEvent.change(screen.getByLabelText(/first name/i), {
      target: { value: "John" },
    });
    fireEvent.change(screen.getByLabelText(/last name/i), {
      target: { value: "Doe" },
    });
    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: "john@example.com" },
    });
    fireEvent.change(screen.getByLabelText(/^password$/i), {
      target: { value: "StrongP@ss1" },
    });
    fireEvent.change(screen.getByLabelText(/confirm password/i), {
      target: { value: "StrongP@ss1" },
    });
    fireEvent.click(
      screen.getByRole("button", { name: /create account/i }),
    );
    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith(
        "john@example.com",
        "StrongP@ss1",
        "John",
        "Doe",
      );
      expect(mockPush).toHaveBeenCalledWith("/dashboard");
    });
  });

  it("displays error message on registration failure", async () => {
    mockRegister.mockRejectedValueOnce({
      response: { data: { message: "Email already in use" } },
    });
    renderWithProviders(<RegisterForm />);
    fireEvent.change(screen.getByLabelText(/first name/i), {
      target: { value: "John" },
    });
    fireEvent.change(screen.getByLabelText(/last name/i), {
      target: { value: "Doe" },
    });
    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: "existing@example.com" },
    });
    fireEvent.change(screen.getByLabelText(/^password$/i), {
      target: { value: "StrongP@ss1" },
    });
    fireEvent.change(screen.getByLabelText(/confirm password/i), {
      target: { value: "StrongP@ss1" },
    });
    fireEvent.click(
      screen.getByRole("button", { name: /create account/i }),
    );
    await waitFor(() => {
      expect(
        screen.getByText(/email already in use/i),
      ).toBeInTheDocument();
    });
  });
});
