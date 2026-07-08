"use client";

import { useEffect, useState, useCallback } from "react";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { developerService } from "@/services/developer-service";
import {
  FolderOpen,
  Key,
  Activity,
  BarChart3,
  Gauge,
  Plus,
  ArrowRight,
  AlertCircle,
  Layers,
} from "lucide-react";
import type { ProjectResponse } from "@/types/api";

export default function DeveloperDashboardPage() {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    try {
      setError(null);
      const data = await developerService.listProjects();
      setProjects(data || []);
    } catch {
      setError("Failed to load developer dashboard");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const totalKeys = projects.reduce(
    (sum) => sum + 0,
    0,
  );

  if (loading) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Developer Portal</h2>
          <p className="text-muted-foreground">
            Manage your projects, API keys, and usage
          </p>
        </div>
        <div className="grid gap-4 md:grid-cols-3">
          {[1, 2, 3].map((i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <Skeleton className="h-8 w-16 mb-2" />
                <Skeleton className="h-4 w-24" />
              </CardContent>
            </Card>
          ))}
        </div>
        <div className="space-y-4">
          {[1, 2].map((i) => (
            <Skeleton key={i} className="h-20 w-full" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Developer Portal</h2>
          <p className="text-muted-foreground">
            Manage your projects, API keys, and usage
          </p>
        </div>
        <Link href="/dashboard/developer/projects/new">
          <Button>
            <Plus className="mr-2 h-4 w-4" />
            New Project
          </Button>
        </Link>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-primary/10 p-3">
              <FolderOpen className="h-6 w-6 text-primary" />
            </div>
            <div>
              <p className="text-2xl font-bold">{projects.length}</p>
              <p className="text-sm text-muted-foreground">Total Projects</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-primary/10 p-3">
              <Key className="h-6 w-6 text-primary" />
            </div>
            <div>
              <p className="text-2xl font-bold">{totalKeys}</p>
              <p className="text-sm text-muted-foreground">API Keys</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-primary/10 p-3">
              <Gauge className="h-6 w-6 text-primary" />
            </div>
            <div>
              <p className="text-2xl font-bold">--</p>
              <p className="text-sm text-muted-foreground">Rate Limit Status</p>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <Activity className="h-4 w-4" />
              API Usage
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <p className="text-sm text-muted-foreground">
                Select a project to view detailed usage analytics including request
                counts, success rates, and latency metrics.
              </p>
              <Link href="/dashboard/developer/projects">
                <Button variant="outline" size="sm">
                  View Projects
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <BarChart3 className="h-4 w-4" />
              Quick Actions
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Link
              href="/dashboard/developer/projects/new"
              className="flex items-center gap-3 rounded-lg border p-3 text-sm hover:bg-accent transition-colors"
            >
              <Plus className="h-4 w-4 text-muted-foreground" />
              Create a new project
            </Link>
            <Link
              href="/dashboard/developer/projects"
              className="flex items-center gap-3 rounded-lg border p-3 text-sm hover:bg-accent transition-colors"
            >
              <Layers className="h-4 w-4 text-muted-foreground" />
              Browse all projects
            </Link>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Recent Projects</CardTitle>
        </CardHeader>
        <CardContent>
          {projects.length === 0 ? (
            <div className="flex flex-col items-center py-8">
              <FolderOpen className="h-8 w-8 text-muted-foreground" />
              <p className="mt-2 text-sm text-muted-foreground">No projects yet</p>
              <Link href="/dashboard/developer/projects/new" className="mt-2">
                <Button variant="outline" size="sm">
                  Create your first project
                </Button>
              </Link>
            </div>
          ) : (
            <div className="space-y-2">
              {projects.slice(0, 5).map((project) => (
                <Link
                  key={project.id}
                  href={`/dashboard/developer/projects/${project.id}`}
                  className="flex items-center justify-between rounded-lg border p-3 hover:bg-accent transition-colors"
                >
                  <div>
                    <p className="text-sm font-medium">{project.name}</p>
                    {project.description && (
                      <p className="text-xs text-muted-foreground truncate max-w-md">
                        {project.description}
                      </p>
                    )}
                  </div>
                  <ArrowRight className="h-4 w-4 text-muted-foreground" />
                </Link>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
