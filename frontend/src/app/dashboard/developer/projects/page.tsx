"use client";

import { useEffect, useState, useCallback } from "react";
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
  FolderOpen,
  Plus,
  Calendar,
  Search,
  AlertCircle,
  ArrowRight,
  Trash2,
} from "lucide-react";
import type { ProjectResponse } from "@/types/api";

export default function ProjectsPage() {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const { toasts, addToast, removeToast } = useToast();

  const loadProjects = useCallback(async () => {
    try {
      setError(null);
      const data = await developerService.listProjects();
      setProjects(data || []);
    } catch {
      setError("Failed to load projects");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadProjects();
  }, [loadProjects]);

  const handleDelete = async (id: string) => {
    setDeletingId(id);
    try {
      await developerService.deleteProject(id);
      setProjects((prev) => prev.filter((p) => p.id !== id));
      addToast({ title: "Project deleted", variant: "success" });
    } catch {
      addToast({ title: "Failed to delete project", variant: "destructive" });
    } finally {
      setDeletingId(null);
    }
  };

  const filtered = search
    ? projects.filter(
        (p) =>
          p.name.toLowerCase().includes(search.toLowerCase()) ||
          (p.description && p.description.toLowerCase().includes(search.toLowerCase())),
      )
    : projects;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Projects</h2>
          <p className="text-muted-foreground">
            Manage your developer projects
          </p>
        </div>
        <Link href="/dashboard/developer/projects/new">
          <Button>
            <Plus className="mr-2 h-4 w-4" />
            New Project
          </Button>
        </Link>
      </div>

      <div className="relative max-w-sm">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          placeholder="Search projects..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-9"
        />
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {loading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <div className="space-y-3">
                  <Skeleton className="h-5 w-48" />
                  <Skeleton className="h-4 w-64" />
                  <Skeleton className="h-4 w-24" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <FolderOpen className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">
              {search ? "No projects match your search" : "No projects"}
            </p>
            <p className="text-sm text-muted-foreground">
              {search
                ? "Try adjusting your search terms"
                : "Create your first project to get started"}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {filtered.map((project) => (
            <Card key={project.id}>
              <CardContent className="p-6">
                <div className="flex items-start justify-between">
                  <Link
                    href={`/dashboard/developer/projects/${project.id}`}
                    className="flex items-start gap-4 flex-1"
                  >
                    <div className="mt-1">
                      <FolderOpen className="h-5 w-5 text-muted-foreground" />
                    </div>
                    <div className="space-y-1">
                      <p className="font-medium">{project.name}</p>
                      {project.description && (
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {project.description}
                        </p>
                      )}
                      <div className="flex items-center gap-4 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          Created: {new Date(project.createdAt).toLocaleDateString()}
                        </span>
                        {!project.enabled && (
                          <span className="text-red-500">Disabled</span>
                        )}
                      </div>
                    </div>
                  </Link>
                  <div className="flex items-center gap-2">
                    <Link
                      href={`/dashboard/developer/projects/${project.id}`}
                    >
                      <Button variant="ghost" size="icon">
                        <ArrowRight className="h-4 w-4" />
                      </Button>
                    </Link>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleDelete(project.id)}
                      disabled={deletingId === project.id}
                    >
                      <Trash2 className="h-4 w-4 text-red-500" />
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
