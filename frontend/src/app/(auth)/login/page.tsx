import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { LoginForm } from "@/components/forms/login-form";
import Link from "next/link";

export default function LoginPage() {
  return (
    <Card>
      <CardHeader className="text-center">
        <CardTitle>Welcome Back</CardTitle>
        <p className="text-sm text-muted-foreground">
          Sign in to your account to continue
        </p>
      </CardHeader>
      <CardContent>
        <LoginForm />
        <p className="mt-4 text-center text-sm text-muted-foreground">
          Don&apos;t have an account?{" "}
          <Link
            href="/register"
            className="font-medium text-primary hover:underline"
          >
            Sign up
          </Link>
        </p>
      </CardContent>
    </Card>
  );
}
