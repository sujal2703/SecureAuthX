"use client";

import { useEffect, useState, useCallback } from "react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useToast } from "@/hooks/use-toast";
import { ToastContainer } from "@/components/ui/toast";
import { organizationService } from "@/services/organization-service";
import {
  Building2,
  Shield,
  Calendar,
  Plus,
  Pencil,
  CheckCircle2,
  AlertCircle,
  Star,
  Users,
} from "lucide-react";
import type { Organization } from "@/types/api";

export default function OrganizationsPage() {
  const [orgs, setOrgs] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);
  const [newOrgName, setNewOrgName] = useState("");
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");
  const [saving, setSaving] = useState(false);
  const { toasts, addToast, removeToast } = useToast();

  const loadOrgs = useCallback(async () => {
    try {
      setError(null);
      const data = await organizationService.list();
      setOrgs(data || []);
    } catch {
      setError("Failed to load organizations");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadOrgs();
  }, [loadOrgs]);

  const handleCreate = async () => {
    if (!newOrgName.trim()) return;
    setSaving(true);
    try {
      const created = await organizationService.create({ name: newOrgName.trim() });
      setOrgs((prev) => [...prev, created]);
      setNewOrgName("");
      setCreating(false);
      addToast({ title: "Organization created", variant: "success" });
    } catch {
      addToast({ title: "Failed to create organization", variant: "destructive" });
    } finally {
      setSaving(false);
    }
  };

  const handleUpdate = async (id: string) => {
    if (!editName.trim()) return;
    setSaving(true);
    try {
      const updated = await organizationService.update(id, { name: editName.trim() });
      setOrgs((prev) => prev.map((o) => (o.id === id ? updated : o)));
      setEditingId(null);
      setEditName("");
      addToast({ title: "Organization updated", variant: "success" });
    } catch {
      addToast({ title: "Failed to update organization", variant: "destructive" });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Organizations</h2>
          <p className="text-muted-foreground">
            Manage your organizations and teams
          </p>
        </div>
        <Button onClick={() => setCreating(true)} disabled={creating}>
          <Plus className="mr-2 h-4 w-4" />
          New Organization
        </Button>
      </div>

      {creating && (
        <Card>
          <CardContent className="flex items-center gap-3 p-4">
            <Input
              placeholder="Organization name"
              value={newOrgName}
              onChange={(e) => setNewOrgName(e.target.value)}
              className="flex-1"
              onKeyDown={(e) => {
                if (e.key === "Enter") handleCreate();
                if (e.key === "Escape") setCreating(false);
              }}
              autoFocus
            />
            <Button size="sm" onClick={handleCreate} disabled={saving || !newOrgName.trim()}>
              {saving ? "Creating..." : "Create"}
            </Button>
            <Button size="sm" variant="outline" onClick={() => setCreating(false)}>
              Cancel
            </Button>
          </CardContent>
        </Card>
      )}

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
      ) : orgs.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <Building2 className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">No organizations</p>
            <p className="text-sm text-muted-foreground">
              Create your first organization to get started
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {orgs.map((org) => (
            <Card key={org.id}>
              <CardContent className="p-6">
                <div className="flex items-start justify-between">
                  <div className="flex items-start gap-4">
                    <div className="mt-1">
                      <Building2 className="h-5 w-5 text-muted-foreground" />
                    </div>
                    <div className="space-y-2">
                      <div className="flex items-center gap-2">
                        {editingId === org.id ? (
                          <div className="flex items-center gap-2">
                            <Input
                              value={editName}
                              onChange={(e) => setEditName(e.target.value)}
                              className="h-8 w-48"
                              onKeyDown={(e) => {
                                if (e.key === "Enter") handleUpdate(org.id);
                                if (e.key === "Escape") {
                                  setEditingId(null);
                                  setEditName("");
                                }
                              }}
                              autoFocus
                            />
                            <Button size="sm" onClick={() => handleUpdate(org.id)} disabled={saving}>
                              Save
                            </Button>
                            <Button size="sm" variant="outline" onClick={() => { setEditingId(null); setEditName(""); }}>
                              Cancel
                            </Button>
                          </div>
                        ) : (
                          <>
                            <p className="font-medium">{org.name}</p>
                            {org.personal && (
                              <span className="rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary flex items-center gap-1">
                                <Star className="h-3 w-3" />
                                Personal
                              </span>
                            )}
                          </>
                        )}
                      </div>
                      <div className="flex flex-wrap gap-x-4 gap-y-1 text-sm text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Shield className="h-3.5 w-3.5" />
                          {org.role}
                        </span>
                        <span className="font-mono text-xs">{org.slug}</span>
                      </div>
                      <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          Created: {new Date(org.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                  </div>
                  {!org.personal && editingId !== org.id && (
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => {
                        setEditingId(org.id);
                        setEditName(org.name);
                      }}
                    >
                      <Pencil className="h-4 w-4" />
                    </Button>
                  )}
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
