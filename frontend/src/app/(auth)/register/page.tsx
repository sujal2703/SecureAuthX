import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { RegisterForm } from "@/components/forms/register-form";
import Link from "next/link";

export default function RegisterPage() {
  return (
    <Card>
      <CardHeader className="text-center">
        <CardTitle>Create an Account</CardTitle>
        <p className="text-sm text-muted-foreground">
          Sign up for a new SecureAuthX account
        </p>
      </CardHeader>
      <CardContent>
        <RegisterForm />
        <p className="mt-4 text-center text-sm text-muted-foreground">
          Already have an account?{" "}
          <Link
            href="/login"
            className="font-medium text-primary hover:underline"
          >
            Sign in
          </Link>
        </p>
      </CardContent>
    </Card>
  );
}
