package com.example.creamsy;

public class SupabaseConfig {
    // Supabase project configuration
    private static final String SUPABASE_URL = "https://qcvxiqqwzusidfrrujrd.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFjdnhpcXF3enVzaWRmcnJ1anJkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ5ODEwNzcsImV4cCI6MjA3MDU1NzA3N30.NDDCJb6RoCikVMUZPVyN9X3lgqqIQMyVpLCKXxnwcIw";
    
    public static String getUrl() {
        return SUPABASE_URL;
    }
    
    public static String getApiKey() {
        return SUPABASE_API_KEY;
    }
    
    // TODO: Implement actual Supabase client when Kotlin library API is stable
    // For now, we use SQLite as fallback in SupabaseRepository
}
