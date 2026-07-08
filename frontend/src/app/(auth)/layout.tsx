import { ShieldCheck } from "lucide-react";

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-background to-secondary p-4">
      <div className="w-full max-w-md">
        <div className="mb-8 flex flex-col items-center gap-2">
          <ShieldCheck className="h-12 w-12 text-primary" />
          <h1 className="text-2xl font-bold">SecureAuthX</h1>
          <p className="text-sm text-muted-foreground">
            Enterprise Authentication Platform
          </p>
        </div>
        {children}
      </div>
    </div>
  );
}
