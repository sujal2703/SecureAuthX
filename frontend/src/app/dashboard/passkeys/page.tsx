"use client";

import { useEffect, useState, useCallback } from "react";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useToast } from "@/hooks/use-toast";
import { ToastContainer } from "@/components/ui/toast";
import { passkeyService } from "@/services/passkey-service";
import {
  Fingerprint,
  Plus,
  Smartphone,
  Key,
  Trash2,
  Calendar,
  AlertCircle,
  CheckCircle2,
  Laptop,
} from "lucide-react";
import type { Passkey } from "@/types/api";

export default function PasskeysPage() {
  const [passkeys, setPasskeys] = useState<Passkey[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const { toasts, addToast, removeToast } = useToast();

  const loadPasskeys = useCallback(async () => {
    try {
      setError(null);
      const data = await passkeyService.list();
      setPasskeys(data || []);
    } catch {
      setError("Failed to load passkeys");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPasskeys();
  }, [loadPasskeys]);

  const handleDelete = async (id: string) => {
    setDeletingId(id);
    try {
      await passkeyService.delete(id);
      setPasskeys((prev) => prev.filter((p) => p.id !== id));
      addToast({ title: "Passkey removed", variant: "success" });
    } catch {
      addToast({ title: "Failed to remove passkey", variant: "destructive" });
    } finally {
      setDeletingId(null);
    }
  };

  const webAuthnSupported =
    typeof window !== "undefined" &&
    typeof window.PublicKeyCredential !== "undefined";

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Passkeys</h2>
          <p className="text-muted-foreground">
            Manage your WebAuthn passkeys for passwordless authentication
          </p>
        </div>
        <Link href="/dashboard/passkeys/register">
          <Button disabled={!webAuthnSupported}>
            <Plus className="mr-2 h-4 w-4" />
            Register Passkey
          </Button>
        </Link>
      </div>

      {!webAuthnSupported && (
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            WebAuthn is not supported in your browser. Use a modern browser to register passkeys.
          </AlertDescription>
        </Alert>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-primary/10 p-3">
              <Fingerprint className="h-6 w-6 text-primary" />
            </div>
            <div>
              <p className="text-2xl font-bold">{passkeys.length}</p>
              <p className="text-sm text-muted-foreground">Total Passkeys</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-primary/10 p-3">
              <Laptop className="h-6 w-6 text-primary" />
            </div>
            <div>
              <p className="text-sm font-medium">
                {webAuthnSupported ? "Supported" : "Not Supported"}
              </p>
              <p className="text-xs text-muted-foreground">WebAuthn Support</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {loading ? (
        <div className="space-y-4">
          {[1, 2].map((i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <div className="space-y-3">
                  <Skeleton className="h-5 w-48" />
                  <Skeleton className="h-4 w-32" />
                  <Skeleton className="h-4 w-24" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : passkeys.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <Fingerprint className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">No passkeys registered</p>
            <p className="text-sm text-muted-foreground">
              Register a passkey to enable passwordless authentication
            </p>
            {webAuthnSupported && (
              <Link href="/dashboard/passkeys/register" className="mt-4">
                <Button>
                  <Plus className="mr-2 h-4 w-4" />
                  Register Passkey
                </Button>
              </Link>
            )}
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {passkeys.map((passkey) => (
            <Card key={passkey.id}>
              <CardContent className="p-6">
                <div className="flex items-start justify-between">
                  <div className="flex items-start gap-4">
                    <div className="mt-1">
                      <Smartphone className="h-5 w-5 text-muted-foreground" />
                    </div>
                    <div className="space-y-2">
                      <div className="flex items-center gap-2">
                        <p className="font-medium">
                          {passkey.deviceName || "Unknown Device"}
                        </p>
                        {passkey.backedUp && (
                          <span className="rounded-full bg-green-500/10 px-2 py-0.5 text-xs font-medium text-green-600 flex items-center gap-1">
                            <CheckCircle2 className="h-3 w-3" />
                            Synced
                          </span>
                        )}
                      </div>
                      <div className="flex flex-wrap gap-x-4 gap-y-1 text-sm text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Key className="h-3.5 w-3.5" />
                          {passkey.credentialType || "Passkey"}
                        </span>
                        <span className="font-mono text-xs">
                          ID: {passkey.credentialId.substring(0, 16)}...
                        </span>
                      </div>
                      <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          Registered: {new Date(passkey.createdAt).toLocaleDateString()}
                        </span>
                        <span className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          Updated: {new Date(passkey.updatedAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleDelete(passkey.id)}
                    disabled={deletingId === passkey.id}
                  >
                    <Trash2 className="mr-2 h-4 w-4" />
                    {deletingId === passkey.id ? "Removing..." : "Remove"}
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <ToastContainer toasts={toasts} removeToast={removeToast} />
    </div>
  );
}
