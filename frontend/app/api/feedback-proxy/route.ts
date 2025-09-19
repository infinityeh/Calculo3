import { NextRequest, NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "../../lib/auth-options";

export async function POST(req: NextRequest) {
  const session = await getServerSession(authOptions);
  if (!session?.id_token) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
  const body = await req.json();
  const backendUrl = process.env.BACKEND_URL ?? "http://localhost:8080";
  const response = await fetch(`${backendUrl}/api/feedback`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${session.id_token}`
    },
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    const errorText = await response.text();
    return NextResponse.json({ error: errorText }, { status: response.status });
  }
  return NextResponse.json({ ok: true }, { status: 202 });
}
