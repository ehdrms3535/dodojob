import { serve } from "https://deno.land/std/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
};

serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });

  try {
    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SERVICE_ROLE_KEY")!,   // ← 여기로 변경
    );

    const authHeader = req.headers.get("Authorization") ?? "";
    const jwt = authHeader.startsWith("Bearer ") ? authHeader.slice(7) : null;
    if (!jwt) {
      return new Response(JSON.stringify({ error: "no auth" }), {
        status: 401, headers: { ...cors, "Content-Type": "application/json" },
      });
    }

    const { data: userRes } = await supabase.auth.getUser(jwt);
    const user = userRes?.user;
    if (!user) {
      return new Response(JSON.stringify({ error: "invalid user" }), {
        status: 401, headers: { ...cors, "Content-Type": "application/json" },
      });
    }

    const { token } = await req.json().catch(() => ({}));
    if (!token) {
      return new Response(JSON.stringify({ error: "missing token" }), {
        status: 400, headers: { ...cors, "Content-Type": "application/json" },
      });
    }

    const nowIso = new Date().toISOString();
    const { data: pending, error: selErr } = await supabase
      .from("pending_verifications")
      .select("*").eq("token", token).gt("expires_at", nowIso)
      .maybeSingle();

    if (selErr || !pending) {
      return new Response(JSON.stringify({ error: "token not found or expired" }), {
        status: 400, headers: { ...cors, "Content-Type": "application/json" },
      });
    }

    const { error: upErr } = await supabase.from("verifications").upsert({
      user_id: user.id,
      provider: pending.provider,
      tx_id: pending.tx_id,
      name: pending.name,
      phone: pending.phone,
      birth_date: pending.birth_date,
      gender: pending.gender,
      region: pending.region,
      verified_at: new Date().toISOString(),
    });
    if (upErr) {
      return new Response(JSON.stringify({ error: upErr.message }), {
        status: 400, headers: { ...cors, "Content-Type": "application/json" },
      });
    }

    await supabase.from("pending_verifications").delete().eq("id", pending.id);
    return new Response(JSON.stringify({ ok: true }), {
      headers: { ...cors, "Content-Type": "application/json" },
    });
  } catch (e) {
    return new Response(JSON.stringify({ error: String(e) }), {
      status: 500, headers: { ...cors, "Content-Type": "application/json" },
    });
  }
});
