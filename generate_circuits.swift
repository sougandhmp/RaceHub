#!/usr/bin/env swift
// Generates F1 circuit SVG (iOS) and VectorDrawable XML (Android) for all 25 tracks.
// Transparent background — circuit trace renders over the card background.
// Run from project root:  swift generate_circuits.swift

import Foundation

// ── Config ───────────────────────────────────────────────────────────────────
let W: Double = 640, H: Double = 360
let PAD: Double = 36
let CONTENT_W   = W - 2 * PAD
let CONTENT_H   = H - 2 * PAD
let TRACK: Double = 13
let GLOW: Double  = 8
let SAMPLES       = 24          // catmull-rom samples per segment

// ── Helpers ──────────────────────────────────────────────────────────────────
func px(_ x: Double, _ y: Double) -> (Double, Double) {
    (PAD + x * CONTENT_W, PAD + y * CONTENT_H)
}

func spline(_ raw: [(Double,Double)]) -> [(Double,Double)] {
    var out: [(Double,Double)] = []
    let n = raw.count
    for i in 0..<n {
        let p0 = raw[(i-1+n)%n], p1 = raw[i], p2 = raw[(i+1)%n], p3 = raw[(i+2)%n]
        for j in 0..<SAMPLES {
            let t = Double(j)/Double(SAMPLES), t2 = t*t, t3 = t2*t
            let x = 0.5*((2*p1.0)+(-p0.0+p2.0)*t+(2*p0.0-5*p1.0+4*p2.0-p3.0)*t2+(-p0.0+3*p1.0-3*p2.0+p3.0)*t3)
            let y = 0.5*((2*p1.1)+(-p0.1+p2.1)*t+(2*p0.1-5*p1.1+4*p2.1-p3.1)*t2+(-p0.1+3*p1.1-3*p2.1+p3.1)*t3)
            out.append(px(x, y))
        }
    }
    return out
}

func f(_ v: Double) -> String { String(format: "%.1f", v) }

func trackPath(_ curve: [(Double,Double)]) -> String {
    curve.enumerated().map { i, pt in (i == 0 ? "M" : "L") + "\(f(pt.0)),\(f(pt.1))" }
         .joined(separator: " ") + " Z"
}

// Circle as two arcs (SVG arc syntax, compatible with both SVG and VectorDrawable)
func circlePath(cx: Double, cy: Double, r: Double) -> String {
    "M\(f(cx-r)),\(f(cy)) a\(f(r)),\(f(r)) 0 1,0 \(f(2*r)),0 a\(f(r)),\(f(r)) 0 1,0 \(f(-2*r)),0"
}

// ── SVG renderer (transparent bg, for iOS asset catalog) ─────────────────────
func renderSVG(_ pts: [(Double,Double)], to dest: String) {
    let curve = spline(pts)
    let pd    = trackPath(curve)
    let start = px(pts[0].0, pts[0].1)

    let svg = """
    <?xml version="1.0" encoding="UTF-8"?>
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 \(Int(W)) \(Int(H))">
      <path d="\(pd)" fill="none" stroke="#E63946" stroke-width="\(TRACK+GLOW)" stroke-opacity="0.25" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="\(pd)" fill="none" stroke="#FFFFFF" stroke-width="\(TRACK)" stroke-linecap="round" stroke-linejoin="round"/>
      <circle cx="\(f(start.0))" cy="\(f(start.1))" r="7" fill="#E63946"/>
    </svg>
    """
    try? svg.write(toFile: dest, atomically: true, encoding: .utf8)
}

// ── VectorDrawable renderer (transparent bg, for Android res/drawable) ────────
func renderVD(_ pts: [(Double,Double)], to dest: String) {
    let curve = spline(pts)
    let pd    = trackPath(curve)
    let start = px(pts[0].0, pts[0].1)
    let dot   = circlePath(cx: start.0, cy: start.1, r: 7)

    let xml = """
    <?xml version="1.0" encoding="UTF-8"?>
    <vector xmlns:android="http://schemas.android.com/apk/res/android"
        android:width="\(Int(W))dp"
        android:height="\(Int(H))dp"
        android:viewportWidth="\(Int(W))"
        android:viewportHeight="\(Int(H))">
        <path
            android:pathData="\(pd)"
            android:fillColor="#00000000"
            android:strokeColor="#40E63946"
            android:strokeWidth="\(TRACK+GLOW)"
            android:strokeLineCap="round"
            android:strokeLineJoin="round"/>
        <path
            android:pathData="\(pd)"
            android:fillColor="#00000000"
            android:strokeColor="#FFFFFFFF"
            android:strokeWidth="\(TRACK)"
            android:strokeLineCap="round"
            android:strokeLineJoin="round"/>
        <path
            android:pathData="\(dot)"
            android:fillColor="#FFE63946"/>
    </vector>
    """
    try? xml.write(toFile: dest, atomically: true, encoding: .utf8)
}

// ── Circuit paths ─────────────────────────────────────────────────────────────
// Normalized [0,1] screen coords (x right, y down). Traced in racing direction.
// Designed for 16:9 canvas — spread horizontally, compact vertically.
let circuits: [(String, [(Double,Double)])] = [

    // Bahrain – SF straight bottom-left, three looping sectors, clockwise
    ("bahrain", [
        (0.08,0.72),(0.65,0.72),(0.78,0.62),(0.80,0.50),
        (0.86,0.40),(0.80,0.30),(0.70,0.24),(0.60,0.28),
        (0.65,0.38),(0.72,0.48),(0.65,0.58),(0.52,0.60),
        (0.40,0.54),(0.28,0.58),(0.16,0.66)
    ]),

    // Jeddah Corniche – long SF straight bottom, kink-heavy right wall
    ("jeddah", [
        (0.10,0.84),(0.86,0.84),(0.93,0.73),(0.87,0.62),
        (0.93,0.52),(0.87,0.42),(0.93,0.32),(0.87,0.22),
        (0.86,0.14),(0.14,0.14),(0.07,0.50)
    ]),

    // Albert Park Australia – roughly square loop, clockwise
    ("australia", [
        (0.46,0.88),(0.68,0.82),(0.82,0.70),(0.86,0.52),
        (0.80,0.34),(0.66,0.18),(0.46,0.12),(0.28,0.18),
        (0.14,0.34),(0.10,0.52),(0.16,0.70),(0.30,0.82)
    ]),

    // Suzuka – iconic figure-8 crossover, bottom loop clockwise
    ("japan", [
        (0.50,0.88),(0.27,0.80),(0.15,0.65),(0.18,0.53),
        (0.34,0.51),(0.50,0.50),(0.34,0.49),(0.18,0.47),
        (0.15,0.32),(0.27,0.18),(0.50,0.12),(0.73,0.18),
        (0.85,0.32),(0.82,0.47),(0.66,0.49),(0.50,0.50),
        (0.66,0.51),(0.82,0.53),(0.85,0.65),(0.73,0.80)
    ]),

    // Shanghai – snail spiral top, long hairpin bottom-right
    ("china", [
        (0.24,0.82),(0.62,0.82),(0.77,0.70),(0.82,0.54),
        (0.76,0.40),(0.62,0.32),(0.50,0.35),(0.44,0.44),
        (0.50,0.52),(0.57,0.44),(0.50,0.36),(0.37,0.36),
        (0.26,0.46),(0.20,0.60),(0.22,0.72)
    ]),

    // Miami – stadium infield, outer oval with inner loop
    ("miami", [
        (0.46,0.88),(0.66,0.82),(0.80,0.70),(0.85,0.54),
        (0.78,0.40),(0.64,0.32),(0.60,0.42),(0.68,0.52),
        (0.62,0.62),(0.46,0.65),(0.34,0.58),(0.28,0.44),
        (0.20,0.34),(0.12,0.46),(0.10,0.62),(0.16,0.76),
        (0.32,0.86)
    ]),

    // Imola – elongated tear-drop, Tamburello flat, Rivazza double-right
    ("imola", [
        (0.50,0.88),(0.68,0.82),(0.78,0.70),(0.74,0.57),
        (0.60,0.50),(0.74,0.44),(0.78,0.30),(0.70,0.18),
        (0.50,0.12),(0.28,0.18),(0.20,0.30),(0.24,0.44),
        (0.38,0.50),(0.24,0.57),(0.20,0.70),(0.30,0.82)
    ]),

    // Monaco – Loews hairpin top-left, tunnel right, chicane before SF
    ("monaco", [
        (0.42,0.88),(0.60,0.88),(0.74,0.80),(0.82,0.68),
        (0.86,0.52),(0.80,0.42),(0.87,0.32),(0.82,0.20),
        (0.70,0.14),(0.52,0.10),(0.36,0.15),(0.22,0.28),
        (0.14,0.44),(0.15,0.60),(0.22,0.74),(0.32,0.84)
    ]),

    // Catalunya Spain – long SF straight, tight T1-T2, infield complex
    ("spain", [
        (0.10,0.72),(0.80,0.72),(0.90,0.60),(0.88,0.46),
        (0.80,0.36),(0.64,0.30),(0.52,0.32),(0.42,0.24),
        (0.26,0.18),(0.14,0.28),(0.08,0.46),(0.08,0.62)
    ]),

    // Circuit Gilles Villeneuve – island, two chicanes, hairpin wall
    ("canada", [
        (0.22,0.85),(0.74,0.85),(0.84,0.74),(0.84,0.55),
        (0.74,0.46),(0.60,0.44),(0.74,0.40),(0.84,0.28),
        (0.74,0.16),(0.22,0.16),(0.14,0.32),(0.14,0.70)
    ]),

    // Red Bull Ring Austria – compact, Turns 1–3 dominant climb/drop
    ("austria", [
        (0.36,0.82),(0.62,0.82),(0.76,0.68),(0.80,0.50),
        (0.70,0.32),(0.55,0.18),(0.40,0.20),(0.28,0.36),
        (0.22,0.54),(0.28,0.70)
    ]),

    // Silverstone – Maggots/Becketts W-esses, Copse, Stowe, Vale
    ("silverstone", [
        (0.24,0.80),(0.68,0.80),(0.84,0.68),(0.90,0.54),
        (0.82,0.44),(0.70,0.38),(0.60,0.45),(0.50,0.36),
        (0.60,0.28),(0.72,0.22),(0.82,0.15),(0.64,0.10),
        (0.36,0.10),(0.18,0.18),(0.10,0.38),(0.16,0.58),
        (0.20,0.68)
    ]),

    // Hungaroring – tight, twisty, stadium section
    ("hungary", [
        (0.28,0.84),(0.64,0.84),(0.76,0.72),(0.80,0.56),
        (0.72,0.42),(0.82,0.28),(0.74,0.16),(0.56,0.10),
        (0.38,0.14),(0.24,0.28),(0.16,0.46),(0.18,0.64),
        (0.22,0.76)
    ]),

    // Spa-Francorchamps – Eau Rouge sweep, Pouhon, Bus Stop chicane
    ("belgium", [
        (0.10,0.80),(0.46,0.80),(0.60,0.74),(0.66,0.60),
        (0.58,0.50),(0.44,0.48),(0.52,0.40),(0.62,0.28),
        (0.68,0.16),(0.62,0.10),(0.48,0.08),(0.34,0.12),
        (0.20,0.24),(0.12,0.44),(0.08,0.64)
    ]),

    // Zandvoort – compact, high-banked Arie Luyendyk corner at top
    ("netherlands", [
        (0.36,0.88),(0.60,0.88),(0.74,0.76),(0.80,0.60),
        (0.78,0.44),(0.72,0.28),(0.78,0.14),(0.60,0.08),
        (0.40,0.08),(0.24,0.18),(0.16,0.38),(0.18,0.58),
        (0.24,0.74)
    ]),

    // Monza – near-triangular, two chicanes on main straight
    ("italy", [
        (0.22,0.88),(0.68,0.88),(0.82,0.74),(0.84,0.50),
        (0.76,0.40),(0.68,0.48),(0.60,0.40),(0.68,0.24),
        (0.55,0.10),(0.34,0.10),(0.18,0.24),(0.12,0.48),
        (0.18,0.64),(0.16,0.76)
    ]),

    // Baku City Circuit – very long SF straight, narrow castle sector
    ("azerbaijan", [
        (0.07,0.84),(0.84,0.84),(0.92,0.72),(0.92,0.54),
        (0.84,0.44),(0.68,0.40),(0.84,0.34),(0.92,0.20),
        (0.92,0.10),(0.74,0.08),(0.26,0.08),(0.10,0.22),
        (0.06,0.58),(0.08,0.74)
    ]),

    // Marina Bay Singapore – tight streets, many 90° corners
    ("singapore", [
        (0.32,0.90),(0.62,0.90),(0.76,0.80),(0.82,0.64),
        (0.76,0.50),(0.82,0.38),(0.74,0.26),(0.62,0.16),
        (0.48,0.12),(0.36,0.14),(0.24,0.28),(0.16,0.42),
        (0.12,0.58),(0.18,0.72),(0.26,0.82)
    ]),

    // COTA USA – T1 uphill hairpin, S-curves middle, long back straight
    ("usa", [
        (0.24,0.88),(0.68,0.88),(0.80,0.76),(0.86,0.60),
        (0.80,0.46),(0.66,0.38),(0.80,0.30),(0.86,0.14),
        (0.70,0.08),(0.48,0.06),(0.34,0.12),(0.22,0.26),
        (0.14,0.44),(0.10,0.62),(0.16,0.76)
    ]),

    // Autodromo Hermanos Rodriguez – stadium hairpin, long main straight
    ("mexico", [
        (0.16,0.84),(0.78,0.84),(0.88,0.72),(0.92,0.54),
        (0.84,0.40),(0.68,0.34),(0.82,0.28),(0.90,0.14),
        (0.76,0.08),(0.20,0.08),(0.08,0.24),(0.06,0.54),
        (0.12,0.72)
    ]),

    // Interlagos Brazil – compact, anti-clockwise, Senna S
    ("brazil", [
        (0.55,0.88),(0.72,0.80),(0.80,0.65),(0.78,0.50),
        (0.65,0.40),(0.52,0.38),(0.40,0.44),(0.36,0.55),
        (0.42,0.64),(0.55,0.68),(0.64,0.60),(0.68,0.50),
        (0.60,0.40),(0.48,0.40),(0.38,0.50),(0.36,0.64),
        (0.30,0.52),(0.26,0.38),(0.30,0.22),(0.46,0.12),
        (0.64,0.14),(0.76,0.28),(0.78,0.42)
    ]),

    // Las Vegas Strip – large rectangle, two long straights
    ("las_vegas", [
        (0.10,0.82),(0.84,0.82),(0.92,0.72),(0.92,0.22),
        (0.84,0.12),(0.10,0.12),(0.06,0.22),(0.06,0.72)
    ]),

    // Losail Qatar – flowing, smooth high-speed corners
    ("qatar", [
        (0.26,0.85),(0.66,0.85),(0.82,0.72),(0.88,0.54),
        (0.82,0.36),(0.70,0.22),(0.54,0.16),(0.40,0.20),
        (0.26,0.36),(0.16,0.52),(0.18,0.70)
    ]),

    // Yas Marina Abu Dhabi – horseshoe marina section, hotel tunnel
    ("abu_dhabi", [
        (0.24,0.85),(0.68,0.85),(0.82,0.72),(0.86,0.55),
        (0.78,0.40),(0.60,0.33),(0.60,0.44),(0.70,0.52),
        (0.60,0.60),(0.44,0.62),(0.30,0.52),(0.24,0.40),
        (0.16,0.54),(0.12,0.68),(0.18,0.78)
    ]),

    // Madrid IFEMA – clockwise street circuit (2026)
    ("madrid", [
        (0.14,0.82),(0.74,0.82),(0.86,0.72),(0.88,0.56),
        (0.80,0.44),(0.66,0.38),(0.76,0.30),(0.82,0.18),
        (0.72,0.10),(0.54,0.08),(0.36,0.10),(0.22,0.20),
        (0.12,0.34),(0.08,0.54),(0.10,0.68),(0.12,0.76)
    ]),
]

// ── Output paths ──────────────────────────────────────────────────────────────
let root       = URL(fileURLWithPath: FileManager.default.currentDirectoryPath)
let androidDir = root.appendingPathComponent("composeApp/src/androidMain/res/drawable")
let iosAssets  = root.appendingPathComponent("iosApp/iosApp/Assets.xcassets/Circuits")
let fm         = FileManager.default

try? fm.createDirectory(at: iosAssets, withIntermediateDirectories: true)
let nsContents = #"{"info":{"author":"xcode","version":1},"properties":{"provides-namespace":true}}"#
try? nsContents.write(to: iosAssets.appendingPathComponent("Contents.json"), atomically: true, encoding: .utf8)

for (name, pts) in circuits {
    // Android — VectorDrawable XML (transparent bg, loaded via painterResource)
    renderVD(pts, to: androidDir.appendingPathComponent("circuit_\(name).xml").path)
    // Remove old PNG so only the XML resource exists
    try? fm.removeItem(at: androidDir.appendingPathComponent("circuit_\(name).png"))

    // iOS — SVG in imageset (transparent bg)
    let imagesetDir = iosAssets.appendingPathComponent("circuit_\(name).imageset")
    try? fm.createDirectory(at: imagesetDir, withIntermediateDirectories: true)
    let contents = """
    {"images":[{"idiom":"universal","filename":"circuit_\(name).svg"}],"info":{"author":"xcode","version":1},"properties":{"preserves-vector-representation":true}}
    """
    try? contents.write(to: imagesetDir.appendingPathComponent("Contents.json"), atomically: true, encoding: .utf8)
    // Remove old PNG
    try? fm.removeItem(at: imagesetDir.appendingPathComponent("circuit_\(name).png"))
    renderSVG(pts, to: imagesetDir.appendingPathComponent("circuit_\(name).svg").path)

    print("✓ \(name)")
}
print("Done — \(circuits.count) circuits generated.")