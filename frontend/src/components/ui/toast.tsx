"use client";

import { useEffect, useState } from "react";
import { cn } from "@/lib/utils";
import { X } from "lucide-react";
import type { Toast as ToastType } from "@/hooks/use-toast";

interface ToastContainerProps {
  toasts: ToastType[];
  removeToast: (id: string) => void;
}

export function ToastContainer({
  toasts,
  removeToast,
}: ToastContainerProps) {
  if (toasts.length === 0) return null;

  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
      {toasts.map((toast) => (
        <ToastItem
          key={toast.id}
          toast={toast}
          onClose={() => removeToast(toast.id)}
        />
      ))}
    </div>
  );
}

interface ToastItemProps {
  toast: ToastType;
  onClose: () => void;
}

function ToastItem({ toast, onClose }: ToastItemProps) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => setVisible(true), 10);
    return () => clearTimeout(timer);
  }, []);

  const variantStyles = {
    default:
      "bg-background border-border text-foreground",
    destructive:
      "bg-destructive border-destructive text-destructive-foreground",
    success:
      "bg-green-600 border-green-600 text-white",
  };

  return (
    <div
      className={cn(
        "flex items-center gap-3 rounded-lg border px-4 py-3 shadow-lg transition-all duration-300",
        variantStyles[toast.variant || "default"],
        visible
          ? "translate-x-0 opacity-100"
          : "translate-x-full opacity-0",
      )}
    >
      <div className="flex-1">
        <p className="text-sm font-medium">{toast.title}</p>
        {toast.description && (
          <p className="text-sm opacity-90">{toast.description}</p>
        )}
      </div>
      <button
        onClick={onClose}
        className="rounded-md p-1 hover:bg-black/10 transition-colors"
      >
        <X className="h-4 w-4" />
      </button>
    </div>
  );
}
