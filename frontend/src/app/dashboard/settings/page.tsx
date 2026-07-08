"use client";

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Settings as SettingsIcon, Mail, Bell, Shield, Palette } from "lucide-react";

const sections = [
  {
    title: "Email Notifications",
    description: "Configure your email notification preferences",
    icon: Mail,
  },
  {
    title: "Push Notifications",
    description: "Manage push notification settings",
    icon: Bell,
  },
  {
    title: "Security Preferences",
    description: "Update your security and privacy settings",
    icon: Shield,
  },
  {
    title: "Appearance",
    description: "Customize the look and feel of your dashboard",
    icon: Palette,
  },
];

export default function SettingsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Settings</h2>
        <p className="text-muted-foreground">
          Manage your account preferences
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        {sections.map((section) => {
          const Icon = section.icon;
          return (
            <Card key={section.title} className="opacity-60">
              <CardHeader className="flex flex-row items-start gap-4">
                <div className="mt-1">
                  <Icon className="h-5 w-5 text-muted-foreground" />
                </div>
                <div>
                  <CardTitle className="text-base">{section.title}</CardTitle>
                  <p className="mt-1 text-sm text-muted-foreground">
                    {section.description}
                  </p>
                </div>
              </CardHeader>
              <CardContent>
                <p className="text-xs text-muted-foreground italic">
                  Coming soon
                </p>
              </CardContent>
            </Card>
          );
        })}
      </div>
    </div>
  );
}
