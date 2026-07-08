"use client";

import { useEffect, useState, useCallback } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useToast } from "@/hooks/use-toast";
import { ToastContainer } from "@/components/ui/toast";
import { adminService } from "@/services/admin-service";
import {
  Settings2,
  AlertCircle,
  ShieldOff,
  Save,
} from "lucide-react";
import type { SystemSettingResponse } from "@/types/api";

export default function SystemSettingsPage() {
  const [settings, setSettings] = useState<SystemSettingResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAdmin, setIsAdmin] = useState(true);
  const [editValues, setEditValues] = useState<Record<string, string>>({});
  const [savingKey, setSavingKey] = useState<string | null>(null);
  const { toasts, addToast, removeToast } = useToast();

  const loadSettings = useCallback(async () => {
    try {
      setError(null);
      const result = (await adminService.listSettings()) || [];
      setSettings(result);
      const vals: Record<string, string> = {};
      result.forEach((s) => { vals[s.settingKey] = s.settingValue; });
      setEditValues(vals);
    } catch (err: unknown) {
      if (err && typeof err === "object") {
        const axiosErr = err as { response?: { status?: number } };
        if (axiosErr.response?.status === 403) {
          setIsAdmin(false);
          return;
        }
      }
      setError("Failed to load system settings");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSettings();
  }, [loadSettings]);

  const handleSave = async (key: string) => {
    setSavingKey(key);
    try {
      await adminService.updateSetting(key, { settingValue: editValues[key] });
      setSettings((prev) =>
        prev.map((s) => (s.settingKey === key ? { ...s, settingValue: editValues[key] } : s))
      );
      addToast({ title: `Setting "${key}" updated`, variant: "success" });
    } catch {
      addToast({ title: `Failed to update "${key}"`, variant: "destructive" });
    } finally {
      setSavingKey(null);
    }
  };

  if (!isAdmin) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">System Settings</h2>
          <p className="text-muted-foreground">Platform configuration</p>
        </div>
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <ShieldOff className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">Admin access required</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">System Settings</h2>
        <p className="text-muted-foreground">Platform configuration</p>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="h-20 w-full" />
          ))}
        </div>
      ) : settings.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <Settings2 className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">No settings found</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {settings.map((setting) => (
            <Card key={setting.settingKey}>
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-medium">{setting.settingKey}</span>
                    </div>
                    {setting.description && (
                      <p className="text-xs text-muted-foreground">{setting.description}</p>
                    )}
                    <Input
                      value={editValues[setting.settingKey] || ""}
                      onChange={(e) =>
                        setEditValues((prev) => ({ ...prev, [setting.settingKey]: e.target.value }))
                      }
                      className="mt-1 max-w-md"
                    />
                  </div>
                  <Button
                    size="sm"
                    onClick={() => handleSave(setting.settingKey)}
                    disabled={savingKey === setting.settingKey}
                  >
                    <Save className="h-4 w-4 mr-1" />
                    {savingKey === setting.settingKey ? "Saving..." : "Save"}
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
