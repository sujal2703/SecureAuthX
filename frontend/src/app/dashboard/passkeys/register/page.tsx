"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Input } from "@/components/ui/input";
import { passkeyService } from "@/services/passkey-service";
import {
  ArrowLeft,
  Fingerprint,
  AlertCircle,
  CheckCircle2,
  Laptop,
  Smartphone,
  ShieldCheck,
} from "lucide-react";

function base64UrlToBase64(base64Url: string): string {
  return base64Url.replace(/-/g, "+").replace(/_/g, "/");
}

function base64ToBase64Url(base64: string): string {
  return base64.replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function arrayBufferToBase64Url(buffer: ArrayBuffer): string {
  const bytes = new Uint8Array(buffer);
  let binary = "";
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary)
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
}

function base64UrlToArrayBuffer(base64Url: string): ArrayBuffer {
  const padding = "=".repeat((4 - (base64Url.length % 4)) % 4);
  const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/") + padding;
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes.buffer;
}

export default function RegisterPasskeyPage() {
  const router = useRouter();
  const [deviceName, setDeviceName] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [registering, setRegistering] = useState(false);
  const [success, setSuccess] = useState(false);
  const [credentialId, setCredentialId] = useState("");

  const webAuthnSupported =
    typeof window !== "undefined" &&
    typeof window.PublicKeyCredential !== "undefined";

  const handleRegister = async () => {
    if (!webAuthnSupported) {
      setError("WebAuthn is not supported in your browser");
      return;
    }

    setRegistering(true);
    setError(null);

    try {
      const options = await passkeyService.registerOptions();

      const challenge = base64UrlToArrayBuffer(options.challenge);
      const userId = base64UrlToArrayBuffer(options.user.id);

      const publicKey: PublicKeyCredentialCreationOptions = {
        challenge,
        rp: {
          name: options.rp.name,
          id: options.rp.id,
        },
        user: {
          id: userId,
          name: options.user.name,
          displayName: options.user.displayName,
        },
        pubKeyCredParams: options.pubKeyCredParams.map((p) => ({
          type: p.type as PublicKeyCredentialType,
          alg: p.alg,
        })),
        authenticatorSelection: {
          residentKey: options.authenticatorSelection
            .residentKey as ResidentKeyRequirement,
          userVerification: options.authenticatorSelection
            .userVerification as UserVerificationRequirement,
          requireResidentKey: options.authenticatorSelection.requireResidentKey,
        },
        attestation: options.attestation.fmt as AttestationConveyancePreference,
      };

      const credential = (await navigator.credentials.create({
        publicKey,
      })) as PublicKeyCredential;

      if (!credential) {
        throw new Error("Registration was cancelled");
      }

      const response = credential.response as AuthenticatorAttestationResponse;
      const clientDataJSON = arrayBufferToBase64Url(response.clientDataJSON);
      const attestationObject = arrayBufferToBase64Url(
        response.attestationObject,
      );

      let publicKeyBytes: ArrayBuffer | undefined;
      let publicKeyAlgorithm: number | undefined;

      if (response.getPublicKey) {
        const key = response.getPublicKey();
        if (key) publicKeyBytes = key;
      }
      if (response.getPublicKeyAlgorithm) {
        publicKeyAlgorithm = response.getPublicKeyAlgorithm();
      }

      const transports = response.getTransports
        ? response.getTransports().join(",")
        : "";

      const authDataBuffer = response.getAuthenticatorData
        ? response.getAuthenticatorData()
        : new ArrayBuffer(0);
      const authenticatorData = arrayBufferToBase64Url(authDataBuffer);

      const verifyResult = await passkeyService.registerVerify({
        id: credential.id,
        rawId: arrayBufferToBase64Url(credential.rawId),
        type: credential.type,
        clientDataJSON,
        attestationObject,
        authenticatorData,
        publicKey: publicKeyBytes
          ? arrayBufferToBase64Url(publicKeyBytes)
          : "",
        publicKeyAlgorithm: publicKeyAlgorithm?.toString() || "",
        transports,
        aaguid: "",
        deviceName: deviceName.trim() || "Unknown Device",
      });

      if (verifyResult.verified) {
        setSuccess(true);
        setCredentialId(verifyResult.credentialId);
      } else {
        setError("Passkey verification failed");
      }
    } catch (err: unknown) {
      if (err instanceof Error) {
        if (err.name === "AbortError" || err.name === "NotAllowedError") {
          setError("Registration was cancelled");
        } else {
          setError(err.message || "Failed to register passkey");
        }
      } else {
        setError("An unexpected error occurred");
      }
    } finally {
      setRegistering(false);
    }
  };

  if (!webAuthnSupported) {
    return (
      <div className="space-y-6">
        <Link
          href="/dashboard/passkeys"
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Passkeys
        </Link>
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Register Passkey</h2>
          <p className="text-muted-foreground">
            WebAuthn is not supported in your browser
          </p>
        </div>
        <Card>
          <CardContent className="flex flex-col items-center py-12">
            <Smartphone className="h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">Unsupported Browser</p>
            <p className="text-sm text-muted-foreground text-center max-w-md">
              Your browser does not support WebAuthn. Please use a modern browser
              like Chrome, Firefox, Edge, or Safari to register a passkey.
            </p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (success) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Passkey Registered</h2>
          <p className="text-muted-foreground">
            Your passkey was registered successfully
          </p>
        </div>
        <Card className="border-green-500/50">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-green-600">
              <CheckCircle2 className="h-5 w-5" />
              Registration Complete
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-3">
              <Fingerprint className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">{deviceName || "Unknown Device"}</p>
                <p className="text-xs text-muted-foreground font-mono">
                  Credential ID: {credentialId.substring(0, 24)}...
                </p>
              </div>
            </div>
            <div className="flex gap-3 pt-2">
              <Link href="/dashboard/passkeys">
                <Button variant="outline">View Passkeys</Button>
              </Link>
              <Button onClick={() => router.push("/dashboard/passkeys")}>
                Done
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <Link
        href="/dashboard/passkeys"
        className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to Passkeys
      </Link>

      <div>
        <h2 className="text-2xl font-bold tracking-tight">Register Passkey</h2>
        <p className="text-muted-foreground">
          Create a new passkey for passwordless authentication
        </p>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <Card>
        <CardContent className="p-6 space-y-4">
          <div className="space-y-2">
            <label htmlFor="deviceName" className="text-sm font-medium leading-none">
              Device Name
            </label>
            <Input
              id="deviceName"
              placeholder="e.g. My Laptop"
              value={deviceName}
              onChange={(e) => setDeviceName(e.target.value)}
            />
          </div>

          <div className="rounded-lg border p-4 space-y-3">
            <p className="text-sm font-medium flex items-center gap-2">
              <ShieldCheck className="h-4 w-4 text-primary" />
              WebAuthn Status
            </p>
            <div className="flex items-center gap-3 text-sm">
              <div className="flex items-center gap-2">
                <CheckCircle2 className="h-4 w-4 text-green-500" />
                <span>WebAuthn Supported</span>
              </div>
            </div>
            <div className="flex items-center gap-3 text-sm">
              <div className="flex items-center gap-2">
                <Laptop className="h-4 w-4 text-muted-foreground" />
                <span>Platform authenticator available</span>
              </div>
            </div>
          </div>

          <Button
            onClick={handleRegister}
            disabled={registering}
            className="w-full"
          >
            {registering ? (
              <>
                <Fingerprint className="mr-2 h-4 w-4 animate-pulse" />
                Registering...
              </>
            ) : (
              <>
                <Fingerprint className="mr-2 h-4 w-4" />
                Start Registration
              </>
            )}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
