"use client";

import { useEffect, useState, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useToast } from "@/hooks/use-toast";
import { ToastContainer } from "@/components/ui/toast";
import { developerService } from "@/services/developer-service";
import {
  ArrowLeft,
  FolderOpen,
  Key,
  Activity,
  Gauge,
  Plus,
  Trash2,
  Copy,
  CheckCircle2,
  AlertCircle,
  Calendar,
  Clock,
  ShieldAlert,
  Eye,
  EyeOff,
  RefreshCw,
} from "lucide-react";
import type {
  ProjectResponse,
  ApiKeyResponse,
  CreateApiKeyResponse,
  UsageAnalyticsResponse,
  RateLimitResponse,
} from "@/types/api";

type Tab = "overview" | "api-keys" | "usage" | "rate-limits";

export default function ProjectDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;
  const [project, setProject] = useState<ProjectResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<Tab>("overview");
  const { toasts, addToast, removeToast } = useToast();

  // API Keys state
  const [apiKeys, setApiKeys] = useState<ApiKeyResponse[]>([]);
  const [loadingKeys, setLoadingKeys] = useState(false);
  const [showCreateKey, setShowCreateKey] = useState(false);
  const [newKeyLabel, setNewKeyLabel] = useState("");
  const [creatingKey, setCreatingKey] = useState(false);
  const [createdKey, setCreatedKey] = useState<CreateApiKeyResponse | null>(null);
  const [showKeyValue, setShowKeyValue] = useState(false);
  const [copied, setCopied] = useState(false);
  const [revokingId, setRevokingId] = useState<string | null>(null);

  // Usage state
  const [usage, setUsage] = useState<UsageAnalyticsResponse[]>([]);
  const [loadingUsage, setLoadingUsage] = useState(false);

  // Rate limits state
  const [rateLimits, setRateLimits] = useState<RateLimitResponse | null>(null);
  const [loadingRateLimits, setLoadingRateLimits] = useState(false);
  const [editingRateLimits, setEditingRateLimits] = useState(false);
  const [rpm, setRpm] = useState(100);
  const [rph, setRph] = useState(1000);
  const [savingRateLimits, setSavingRateLimits] = useState(false);

  const loadProject = useCallback(async () => {
    try {
      setError(null);
      const data = await developerService.getProject(id);
      setProject(data);
    } catch {
      setError("Failed to load project");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadProject();
  }, [loadProject]);

  const loadApiKeys = useCallback(async () => {
    setLoadingKeys(true);
    try {
      const data = await developerService.listApiKeys(id);
      setApiKeys(data || []);
    } catch {
      // silent
    } finally {
      setLoadingKeys(false);
    }
  }, [id]);

  const loadUsage = useCallback(async () => {
    setLoadingUsage(true);
    try {
      const data = await developerService.getUsage(id);
      setUsage(data || []);
    } catch {
      // silent
    } finally {
      setLoadingUsage(false);
    }
  }, [id]);

  const loadRateLimits = useCallback(async () => {
    setLoadingRateLimits(true);
    try {
      const data = await developerService.getRateLimits(id);
      setRateLimits(data);
    } catch {
      setRateLimits(null);
    } finally {
      setLoadingRateLimits(false);
    }
  }, [id]);

  useEffect(() => {
    if (activeTab === "api-keys") loadApiKeys();
    if (activeTab === "usage") loadUsage();
    if (activeTab === "rate-limits") {
      loadRateLimits();
    }
  }, [activeTab, loadApiKeys, loadUsage, loadRateLimits]);

  const handleCreateKey = async () => {
    if (!newKeyLabel.trim()) return;
    setCreatingKey(true);
    try {
      const result = await developerService.createApiKey(id, {
        label: newKeyLabel.trim(),
      });
      setCreatedKey(result);
      setNewKeyLabel("");
      loadApiKeys();
    } catch {
      addToast({ title: "Failed to create API key", variant: "destructive" });
    } finally {
      setCreatingKey(false);
    }
  };

  const handleRevokeKey = async (keyId: string) => {
    setRevokingId(keyId);
    try {
      await developerService.revokeApiKey(id, keyId);
      setApiKeys((prev) => prev.filter((k) => k.id !== keyId));
      addToast({ title: "API key revoked", variant: "success" });
    } catch {
      addToast({ title: "Failed to revoke API key", variant: "destructive" });
    } finally {
      setRevokingId(null);
    }
  };

  const copyToClipboard = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {}
  };

  const handleSaveRateLimits = async () => {
    setSavingRateLimits(true);
    try {
      const result = await developerService.setRateLimits(id, {
        requestsPerMinute: rpm,
        requestsPerHour: rph,
      });
      setRateLimits(result);
      setEditingRateLimits(false);
      addToast({ title: "Rate limits updated", variant: "success" });
    } catch {
      addToast({ title: "Failed to update rate limits", variant: "destructive" });
    } finally {
      setSavingRateLimits(false);
    }
  };

  const handleDeleteRateLimits = async () => {
    try {
      await developerService.deleteRateLimits(id);
      setRateLimits(null);
      addToast({ title: "Rate limits disabled", variant: "success" });
    } catch {
      addToast({ title: "Failed to disable rate limits", variant: "destructive" });
    }
  };

  const tabs: { key: Tab; label: string; icon: typeof FolderOpen }[] = [
    { key: "overview", label: "Overview", icon: FolderOpen },
    { key: "api-keys", label: "API Keys", icon: Key },
    { key: "usage", label: "Usage", icon: Activity },
    { key: "rate-limits", label: "Rate Limits", icon: Gauge },
  ];

  if (loading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-6 w-64" />
        <Skeleton className="h-48 w-full" />
      </div>
    );
  }

  if (error || !project) {
    return (
      <div className="space-y-6">
        <Link
          href="/dashboard/developer/projects"
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Projects
        </Link>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error || "Project not found"}</AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <Link
        href="/dashboard/developer/projects"
        className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to Projects
      </Link>

      <div className="flex items-center gap-3">
        <FolderOpen className="h-6 w-6 text-primary" />
        <div>
          <h2 className="text-2xl font-bold tracking-tight">{project.name}</h2>
          {project.description && (
            <p className="text-muted-foreground">{project.description}</p>
          )}
        </div>
      </div>

      <div className="flex gap-1 rounded-lg border p-1 bg-muted/50">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.key;
          return (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key)}
              className={`flex items-center gap-2 rounded-md px-4 py-2 text-sm font-medium transition-colors ${
                isActive
                  ? "bg-background text-foreground shadow-sm"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              <Icon className="h-4 w-4" />
              {tab.label}
            </button>
          );
        })}
      </div>

      {activeTab === "overview" && (
        <div className="grid gap-6 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Project Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="flex items-center gap-3">
                <FolderOpen className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">{project.name}</p>
                  <p className="text-xs text-muted-foreground">Name</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Calendar className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">
                    {new Date(project.createdAt).toLocaleString()}
                  </p>
                  <p className="text-xs text-muted-foreground">Created</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <ShieldAlert className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">
                    {project.enabled ? "Enabled" : "Disabled"}
                  </p>
                  <p className="text-xs text-muted-foreground">Status</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Quick Actions</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <Button
                variant="outline"
                className="w-full justify-start"
                onClick={() => setActiveTab("api-keys")}
              >
                <Key className="mr-2 h-4 w-4" />
                Manage API Keys
              </Button>
              <Button
                variant="outline"
                className="w-full justify-start"
                onClick={() => setActiveTab("rate-limits")}
              >
                <Gauge className="mr-2 h-4 w-4" />
                Configure Rate Limits
              </Button>
              <Button
                variant="outline"
                className="w-full justify-start"
                onClick={() => setActiveTab("usage")}
              >
                <Activity className="mr-2 h-4 w-4" />
                View Usage
              </Button>
            </CardContent>
          </Card>
        </div>
      )}

      {activeTab === "api-keys" && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold">API Keys</h3>
            {!showCreateKey && !createdKey && (
              <Button size="sm" onClick={() => setShowCreateKey(true)}>
                <Plus className="mr-2 h-4 w-4" />
                New API Key
              </Button>
            )}
          </div>

          {!createdKey && showCreateKey && (
            <Card>
              <CardContent className="flex items-center gap-3 p-4">
                <Input
                  placeholder="Key label (e.g. Production)"
                  value={newKeyLabel}
                  onChange={(e) => setNewKeyLabel(e.target.value)}
                  className="flex-1"
                  onKeyDown={(e) => {
                    if (e.key === "Enter") handleCreateKey();
                    if (e.key === "Escape") setShowCreateKey(false);
                  }}
                  autoFocus
                />
                <Button
                  size="sm"
                  onClick={handleCreateKey}
                  disabled={creatingKey || !newKeyLabel.trim()}
                >
                  {creatingKey ? "Creating..." : "Create"}
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => setShowCreateKey(false)}
                >
                  Cancel
                </Button>
              </CardContent>
            </Card>
          )}

          {createdKey && (
            <Card className="border-green-500/50">
              <CardContent className="p-4 space-y-3">
                <div className="flex items-center gap-2 text-green-600">
                  <CheckCircle2 className="h-5 w-5" />
                  <span className="font-medium">API Key Created</span>
                </div>
                <div className="rounded-lg border border-amber-500/50 bg-amber-500/5 p-3">
                  <p className="text-xs text-muted-foreground mb-1">
                    Copy this key now. You will not be able to see it again.
                  </p>
                  <div className="flex items-center gap-2">
                    <code className="flex-1 rounded border bg-background px-3 py-2 text-xs font-mono break-all">
                      {showKeyValue
                        ? createdKey.plainTextKey
                        : "•".repeat(Math.min(createdKey.plainTextKey.length, 40))}
                    </code>
                    <Button
                      variant="outline"
                      size="icon"
                      onClick={() => setShowKeyValue(!showKeyValue)}
                    >
                      {showKeyValue ? (
                        <EyeOff className="h-4 w-4" />
                      ) : (
                        <Eye className="h-4 w-4" />
                      )}
                    </Button>
                    <Button
                      variant="outline"
                      size="icon"
                      onClick={() => copyToClipboard(createdKey.plainTextKey)}
                    >
                      {copied ? (
                        <CheckCircle2 className="h-4 w-4 text-green-500" />
                      ) : (
                        <Copy className="h-4 w-4" />
                      )}
                    </Button>
                  </div>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setCreatedKey(null);
                    setShowKeyValue(false);
                  }}
                >
                  Done
                </Button>
              </CardContent>
            </Card>
          )}

          {loadingKeys ? (
            <div className="space-y-3">
              {[1, 2].map((i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          ) : apiKeys.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center py-8">
                <Key className="h-8 w-8 text-muted-foreground" />
                <p className="mt-2 text-sm text-muted-foreground">No API keys</p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-3">
              {apiKeys.map((key) => (
                <Card key={key.id}>
                  <CardContent className="p-4">
                    <div className="flex items-start justify-between">
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <p className="text-sm font-medium">{key.label}</p>
                          {key.enabled ? (
                            <span className="rounded-full bg-green-500/10 px-2 py-0.5 text-xs font-medium text-green-600">
                              Active
                            </span>
                          ) : (
                            <span className="rounded-full bg-red-500/10 px-2 py-0.5 text-xs font-medium text-red-600">
                              Revoked
                            </span>
                          )}
                        </div>
                        <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
                          <span className="font-mono">{key.keyPrefix}...</span>
                          <span className="flex items-center gap-1">
                            <Calendar className="h-3 w-3" />
                            Created: {new Date(key.createdAt).toLocaleDateString()}
                          </span>
                          {key.lastUsedAt && (
                            <span className="flex items-center gap-1">
                              <Clock className="h-3 w-3" />
                              Last used: {new Date(key.lastUsedAt).toLocaleDateString()}
                            </span>
                          )}
                          {key.expiresAt && (
                            <span className="flex items-center gap-1">
                              Expires: {new Date(key.expiresAt).toLocaleDateString()}
                            </span>
                          )}
                        </div>
                      </div>
                      {key.enabled && (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleRevokeKey(key.id)}
                          disabled={revokingId === key.id}
                        >
                          <Trash2 className="mr-2 h-4 w-4" />
                          {revokingId === key.id ? "Revoking..." : "Revoke"}
                        </Button>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === "usage" && (
        <div className="space-y-4">
          <h3 className="text-lg font-semibold">API Usage</h3>

          {loadingUsage ? (
            <div className="grid gap-4 md:grid-cols-3">
              {[1, 2, 3].map((i) => (
                <Skeleton key={i} className="h-24 w-full" />
              ))}
            </div>
          ) : usage.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center py-8">
                <Activity className="h-8 w-8 text-muted-foreground" />
                <p className="mt-2 text-sm text-muted-foreground">No usage data available</p>
              </CardContent>
            </Card>
          ) : (
            <>
              <div className="grid gap-4 md:grid-cols-3">
                <Card>
                  <CardContent className="p-4">
                    <p className="text-2xl font-bold">
                      {usage.reduce((s, u) => s + u.requestCount, 0)}
                    </p>
                    <p className="text-xs text-muted-foreground">Total Requests</p>
                  </CardContent>
                </Card>
                <Card>
                  <CardContent className="p-4">
                    <p className="text-2xl font-bold text-green-600">
                      {usage.reduce((s, u) => s + u.successCount, 0)}
                    </p>
                    <p className="text-xs text-muted-foreground">Success</p>
                  </CardContent>
                </Card>
                <Card>
                  <CardContent className="p-4">
                    <p className="text-2xl font-bold text-red-600">
                      {usage.reduce((s, u) => s + u.failureCount, 0)}
                    </p>
                    <p className="text-xs text-muted-foreground">Failed</p>
                  </CardContent>
                </Card>
              </div>

              <Card>
                <CardHeader>
                  <CardTitle className="text-sm font-medium">Daily Breakdown</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {usage.slice(0, 14).reverse().map((u) => (
                      <div
                        key={u.id || u.date}
                        className="flex items-center justify-between rounded-lg border p-3"
                      >
                        <span className="text-sm">{u.date}</span>
                        <div className="flex items-center gap-4 text-xs text-muted-foreground">
                          <span>{u.requestCount} requests</span>
                          <span className="text-green-600">{u.successCount} ok</span>
                          <span className="text-red-600">{u.failureCount} err</span>
                          <span>{u.avgLatencyMs.toFixed(0)}ms avg</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </>
          )}
        </div>
      )}

      {activeTab === "rate-limits" && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold">Rate Limits</h3>
            {!editingRateLimits && rateLimits && rateLimits.enabled && (
              <div className="flex gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => {
                    setRpm(rateLimits.requestsPerMinute);
                    setRph(rateLimits.requestsPerHour);
                    setEditingRateLimits(true);
                  }}
                >
                  Edit
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={handleDeleteRateLimits}
                >
                  Disable
                </Button>
              </div>
            )}
          </div>

          {loadingRateLimits ? (
            <Skeleton className="h-24 w-full" />
          ) : !rateLimits || !rateLimits.enabled ? (
            <Card>
              <CardContent className="flex flex-col items-center py-8">
                <Gauge className="h-8 w-8 text-muted-foreground" />
                <p className="mt-2 text-sm text-muted-foreground">
                  No rate limits configured
                </p>
                <Button
                  size="sm"
                  className="mt-4"
                  onClick={() => {
                    setRpm(100);
                    setRph(1000);
                    setEditingRateLimits(true);
                  }}
                >
                  Configure Rate Limits
                </Button>
              </CardContent>
            </Card>
          ) : editingRateLimits ? (
            <Card>
              <CardContent className="p-4 space-y-4">
                <div className="grid gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">
                      Requests per Minute
                    </label>
                    <Input
                      type="number"
                      min={1}
                      max={10000}
                      value={rpm}
                      onChange={(e) => setRpm(Number(e.target.value))}
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">
                      Requests per Hour
                    </label>
                    <Input
                      type="number"
                      min={1}
                      max={1000000}
                      value={rph}
                      onChange={(e) => setRph(Number(e.target.value))}
                    />
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button
                    onClick={handleSaveRateLimits}
                    disabled={savingRateLimits}
                  >
                    {savingRateLimits ? "Saving..." : "Save"}
                  </Button>
                  <Button
                    variant="outline"
                    onClick={() => setEditingRateLimits(false)}
                  >
                    Cancel
                  </Button>
                </div>
              </CardContent>
            </Card>
          ) : (
            <Card>
              <CardContent className="p-4">
                <div className="grid gap-4 md:grid-cols-2">
                  <div className="flex items-center gap-3 rounded-lg border p-4">
                    <Gauge className="h-5 w-5 text-muted-foreground" />
                    <div>
                      <p className="text-lg font-bold">
                        {rateLimits.requestsPerMinute}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        Requests per Minute
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 rounded-lg border p-4">
                    <Gauge className="h-5 w-5 text-muted-foreground" />
                    <div>
                      <p className="text-lg font-bold">
                        {rateLimits.requestsPerHour}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        Requests per Hour
                      </p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      )}

      <ToastContainer toasts={toasts} removeToast={removeToast} />
    </div>
  );
}
