import SwiftUI

func circuitImageName(circuit: String, grandPrix: String = "") -> String? {
    for name in [circuit, grandPrix] {
        if let match = circuitImageKey(name) { return match }
    }
    return nil
}

private func circuitImageKey(_ name: String) -> String? {
    let key = name.lowercased()
    if key.contains("bahrain")    || key.contains("sakhir")      { return "Circuits/circuit_bahrain" }
    if key.contains("jeddah")     || key.contains("corniche")     { return "Circuits/circuit_jeddah" }
    if key.contains("albert")     || key.contains("melbourne")    { return "Circuits/circuit_australia" }
    if key.contains("suzuka")     || key.contains("japan")        { return "Circuits/circuit_japan" }
    if key.contains("shanghai")   || key.contains("china")        { return "Circuits/circuit_china" }
    if key.contains("miami")                                       { return "Circuits/circuit_miami" }
    if key.contains("imola")      || key.contains("ferrari")      { return "Circuits/circuit_imola" }
    if key.contains("monaco")                                      { return "Circuits/circuit_monaco" }
    if key.contains("barcelona")  || key.contains("catalunya")    { return "Circuits/circuit_spain" }
    if key.contains("villeneuve") || key.contains("canada")       { return "Circuits/circuit_canada" }
    if key.contains("red bull")   || key.contains("austria") || key.contains("spielberg") { return "Circuits/circuit_austria" }
    if key.contains("silverstone") || key.contains("britain")     { return "Circuits/circuit_silverstone" }
    if key.contains("hungaroring") || key.contains("hungary")     { return "Circuits/circuit_hungary" }
    if key.contains("spa")        || key.contains("belgium")      { return "Circuits/circuit_belgium" }
    if key.contains("zandvoort")  || key.contains("netherlands")  { return "Circuits/circuit_netherlands" }
    if key.contains("monza")      || key.contains("italy")        { return "Circuits/circuit_italy" }
    if key.contains("baku")       || key.contains("azerbaijan")   { return "Circuits/circuit_azerbaijan" }
    if key.contains("marina bay") || key.contains("singapore")    { return "Circuits/circuit_singapore" }
    if key.contains("americas")   || key.contains("cota") || key.contains("austin") { return "Circuits/circuit_usa" }
    if key.contains("hermanos")   || key.contains("mexico")       { return "Circuits/circuit_mexico" }
    if key.contains("interlagos") || key.contains("brazil") || key.contains("pace") { return "Circuits/circuit_brazil" }
    if key.contains("las vegas")  || key.contains("strip")        { return "Circuits/circuit_las_vegas" }
    if key.contains("losail")     || key.contains("lusail") || key.contains("qatar") { return "Circuits/circuit_qatar" }
    if key.contains("yas")        || key.contains("abu dhabi")    { return "Circuits/circuit_abu_dhabi" }
    if key.contains("madrid")                                      { return "Circuits/circuit_madrid" }
    return nil
}

struct CircuitImageView: View {
    let circuitName: String
    let grandPrixName: String
    let colors: AppColors
    var height: CGFloat = 100

    private var imageName: String? {
        circuitImageName(circuit: circuitName, grandPrix: grandPrixName)
    }

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 8)
                .fill(colors.cardBorder.opacity(0.2))
            if let name = imageName {
                Image(name)
                    .resizable()
                    .scaledToFit()
                    .padding(4)
            } else {
                Text("🏁")
                    .font(.system(size: 24))
                    .opacity(0.3)
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: height)
    }
}
