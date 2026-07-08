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
  Megaphone,
  AlertCircle,
  ShieldOff,
  Calendar,
  Plus,
  Pencil,
  Trash2,
  X,
} from "lucide-react";
import type { AnnouncementResponse } from "@/types/api";

export default function AnnouncementsPage() {
  const [announcements, setAnnouncements] = useState<AnnouncementResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAdmin, setIsAdmin] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [formTitle, setFormTitle] = useState("");
  const [formMessage, setFormMessage] = useState("");
  const [formSeverity, setFormSeverity] = useState<"INFO" | "WARNING" | "CRITICAL">("INFO");
  const [formActive, setFormActive] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const { toasts, addToast, removeToast } = useToast();

  const loadAnnouncements = useCallback(async () => {
    try {
      setError(null);
      const result = await adminService.listAnnouncements();
      setAnnouncements(result || []);
    } catch (err: unknown) {
      if (err && typeof err === "object") {
        const axiosErr = err as { response?: { status?: number } };
        if (axiosErr.response?.status === 403) {
          setIsAdmin(false);
          return;
        }
      }
      setError("Failed to load announcements");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAnnouncements();
  }, [loadAnnouncements]);

  const resetForm = () => {
    setFormTitle("");
    setFormMessage("");
    setFormSeverity("INFO");
    setFormActive(true);
    setEditingId(null);
    setShowForm(false);
  };

  const handleEdit = (a: AnnouncementResponse) => {
    setFormTitle(a.title);
    setFormMessage(a.message);
    setFormSeverity(a.severity as "INFO" | "WARNING" | "CRITICAL");
    setFormActive(a.active);
    setEditingId(a.id);
    setShowForm(true);
  };

  const handleSave = async () => {
    if (!formTitle.trim() || !formMessage.trim()) {
      addToast({ title: "Title and message are required", variant: "destructive" });
      return;
    }
    setSaving(true);
    try {
      if (editingId) {
        await adminService.updateAnnouncement(editingId, {
          title: formTitle.trim(),
          message: formMessage.trim(),
          severity: formSeverity,
          active: formActive,
        });
        addToast({ title: "Announcement updated", variant: "success" });
      } else {
        await adminService.createAnnouncement({
          title: formTitle.trim(),
          message: formMessage.trim(),
          severity: formSeverity,
          active: formActive,
        });
        addToast({ title: "Announcement created", variant: "success" });
      }
      resetForm();
      loadAnnouncements();
    } catch {
      addToast({ title: `Failed to ${editingId ? "update" : "create"} announcement`, variant: "destructive" });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    setDeletingId(id);
    try {
      await adminService.deleteAnnouncement(id);
      setAnnouncements((prev) => prev.filter((a) => a.id !== id));
      addToast({ title: "Announcement deleted", variant: "success" });
    } catch {
      addToast({ title: "Failed to delete announcement", variant: "destructive" });
    } finally {
      setDeletingId(null);
    }
  };

  const severityColor = (severity: string) => {
    switch (severity.toUpperCase()) {
      case "CRITICAL": return "text-red-600 bg-red-500/10 border-red-200";
      case "WARNING": return "text-amber-600 bg-amber-500/10 border-amber-200";
      default: return "text-blue-600 bg-blue-500/10 border-blue-200";
    }
  };

  if (!isAdmin) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Announcements</h2>
          <p className="text-muted-foreground">Manage platform announcements</p>
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
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Announcements</h2>
          <p className="text-muted-foreground">Manage platform announcements</p>
        </div>
        <Button onClick={() => { resetForm(); setShowForm(true); }}>
          <Plus className="h-4 w-4 mr-2" /> New Announcement
        </Button>
      </div>

      {showForm && (
        <Card className="border-primary/20">
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-lg">{editingId ? "Edit" : "New"} Announcement</CardTitle>
            <Button variant="ghost" size="icon" onClick={resetForm}>
              <X className="h-4 w-4" />
            </Button>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <label className="text-sm font-medium">Title</label>
              <Input value={formTitle} onChange={(e) => setFormTitle(e.target.value)} placeholder="Announcement title" />
            </div>
            <div>
              <label className="text-sm font-medium">Message</label>
              <textarea value={formMessage} onChange={(e) => setFormMessage(e.target.value)} placeholder="Announcement message" rows={3} className="mt-1 flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm" />
            </div>
            <div className="flex gap-4">
              <div className="flex-1">
                <label className="text-sm font-medium">Severity</label>
                <select
                  value={formSeverity}
                  onChange={(e) => setFormSeverity(e.target.value as "INFO" | "WARNING" | "CRITICAL")}
                  className="mt-1 h-10 w-full rounded-md border border-input bg-background px-3 text-sm"
                >
                  <option value="INFO">Info</option>
                  <option value="WARNING">Warning</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              </div>
              <div className="flex items-end">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input type="checkbox" checked={formActive} onChange={(e) => setFormActive(e.target.checked)} className="h-4 w-4" />
                  <span className="text-sm font-medium">Active</span>
                </label>
              </div>
            </div>
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={resetForm}>Cancel</Button>
              <Button onClick={handleSave} disabled={saving}>
                {saving ? "Saving..." : editingId ? "Update" : "Create"}
              </Button>
            </div>
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
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-24 w-full" />
          ))}
        </div>
      ) : announcements.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <Megaphone className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">No announcements</p>
            <p className="text-sm text-muted-foreground">Create your first announcement</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {announcements.map((a) => (
            <Card key={a.id} className={`border-l-4 ${a.active ? "border-l-primary" : "border-l-muted"}`}>
              <CardContent className="p-4">
                <div className="flex items-start justify-between">
                  <div className="space-y-1 flex-1">
                    <div className="flex items-center gap-2">
                      <span className={`rounded-full px-2 py-0.5 text-xs font-medium border ${severityColor(a.severity)}`}>
                        {a.severity}
                      </span>
                      <span className="text-sm font-medium">{a.title}</span>
                      {!a.active && (
                        <span className="text-xs text-muted-foreground">(Inactive)</span>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground">{a.message}</p>
                    <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
                      <span className="flex items-center gap-1">
                        <Calendar className="h-3 w-3" />
                        {new Date(a.createdAt).toLocaleString()}
                      </span>
                      {a.createdBy && <span>by {a.createdBy}</span>}
                    </div>
                  </div>
                  <div className="flex items-center gap-1 shrink-0">
                    <Button variant="ghost" size="icon" onClick={() => handleEdit(a)}>
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleDelete(a.id)}
                      disabled={deletingId === a.id}
                    >
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
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
