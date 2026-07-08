"use client";

import { useEffect, useState, useCallback } from "react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useToast } from "@/hooks/use-toast";
import { ToastContainer } from "@/components/ui/toast";
import { passkeyService } from "@/services/passkey-service";
import {
  Smartphone,
  Key,
  Trash2,
  Calendar,
  AlertCircle,
  CheckCircle2,
  Fingerprint,
} from "lucide-react";
import type { Passkey } from "@/types/api";

export default function DevicesPage() {
  const [passkeys, setPasskeys] = useState<Passkey[]>([]);
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { toasts, addToast, removeToast } = useToast();

  const loadPasskeys = useCallback(async () => {
    try {
      setError(null);
      const data = await passkeyService.list();
      setPasskeys(data);
    } catch {
      setError("Failed to load devices");
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
      addToast({ title: "Device removed", variant: "success" });
    } catch {
      addToast({ title: "Failed to remove device", variant: "destructive" });
    } finally {
      setDeletingId(null);
    }
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString();
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Devices</h2>
        <p className="text-muted-foreground">
          Manage your registered devices and passkeys
        </p>
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
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : passkeys.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <Fingerprint className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">No devices registered</p>
            <p className="text-sm text-muted-foreground">
              Register a passkey to enable passwordless authentication
            </p>
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
                      <Smartphone className="h-5 w-5" />
                    </div>
                    <div className="space-y-2">
                      <p className="font-medium">
                        {passkey.deviceName || "Unknown Device"}
                      </p>
                      <div className="flex flex-wrap gap-x-4 gap-y-1 text-sm text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Key className="h-3.5 w-3.5" />
                          {passkey.credentialType || "Passkey"}
                        </span>
                        {passkey.backedUp && (
                          <span className="rounded-full bg-green-500/10 px-2 py-0.5 text-xs font-medium text-green-600">
                            Backed up
                          </span>
                        )}
                      </div>
                      <div className="flex gap-x-4 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          Registered: {formatDate(passkey.createdAt)}
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
