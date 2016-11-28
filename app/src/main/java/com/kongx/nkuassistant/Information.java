package com.kongx.nkuassistant;

/**
 * Created by kongx on 2016/11/17 0017.
 */

public class Information {
    public static final String PREFS_NAME = "NKUFile";
    static String name;
    static String facultyName;
    static String id;
    static boolean ifAutoLogin;
    static String js = "var biRadixBase = 2;\n" +
            "var biRadixBits = 16;\n" +
            "var bitsPerDigit = biRadixBits;\n" +
            "var biRadix = 1 << 16; // = 2^16 = 65536\n" +
            "var biHalfRadix = biRadix >>> 1;\n" +
            "var biRadixSquared = biRadix * biRadix;\n" +
            "var maxDigitVal = biRadix - 1;\n" +
            "var maxInteger = 9999999999999998;\n" +
            "var maxDigits;\n" +
            "var ZERO_ARRAY;\n" +
            "var bigZero, bigOne;\n" +
            "var result;\n" +
            "function BigInt (flag) {\n" +
            "    if (typeof flag == \"boolean\" && flag == true) {\n" +
            "        this.digits = null;\n" +
            "    } else {\n" +
            "        this.digits = ZERO_ARRAY.slice(0);\n" +
            "    }\n" +
            "    this.isNeg = false;\n" +
            "};\n" +
            "function setMaxDigits (value) {\n" +
            "    maxDigits = value;\n" +
            "    ZERO_ARRAY = new Array(maxDigits);\n" +
            "    for (var iza = 0; iza < ZERO_ARRAY.length; iza++) ZERO_ARRAY[iza] = 0;\n" +
            "    bigZero = new BigInt();\n" +
            "    bigOne = new BigInt();\n" +
            "    bigOne.digits[0] = 1;\n" +
            "};\n" +
            "setMaxDigits(20);\n" +
            "\n" +
            "//The maximum number of digits in base 10 you can convert to an\n" +
            "//integer without JavaScript throwing up on you.\n" +
            "var dpl10 = 15;\n" +
            "\n" +
            "function biFromNumber(i) {\n" +
            "    var result = new BigInt();\n" +
            "    result.isNeg = i < 0;\n" +
            "    i = Math.abs(i);\n" +
            "    var j = 0;\n" +
            "    while (i > 0) {\n" +
            "        result.digits[j++] = i & maxDigitVal;\n" +
            "        i = Math.floor(i / biRadix);\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "//lr10 = 10 ^ dpl10\n" +
            "var lr10 = biFromNumber(1000000000000000);\n" +
            "function biFromDecimal(s) {\n" +
            "    var isNeg = s.charAt(0) == '-';\n" +
            "    var i = isNeg ? 1 : 0;\n" +
            "    var result;\n" +
            "    // Skip leading zeros.\n" +
            "    while (i < s.length && s.charAt(i) == '0') ++i;\n" +
            "    if (i == s.length) {\n" +
            "        result = new BigInt();\n" +
            "    }\n" +
            "    else {\n" +
            "        var digitCount = s.length - i;\n" +
            "        var fgl = digitCount % dpl10;\n" +
            "        if (fgl == 0) fgl = dpl10;\n" +
            "        result = biFromNumber(Number(s.substr(i, fgl)));\n" +
            "        i += fgl;\n" +
            "        while (i < s.length) {\n" +
            "            result = biAdd(biMultiply(result, lr10),\n" +
            "                           biFromNumber(Number(s.substr(i, dpl10))));\n" +
            "            i += dpl10;\n" +
            "        }\n" +
            "        result.isNeg = isNeg;\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "function biCopy (bi) {\n" +
            "    var result = new BigInt(true);\n" +
            "    result.digits = bi.digits.slice(0);\n" +
            "    result.isNeg = bi.isNeg;\n" +
            "    return result;\n" +
            "};\n" +
            "function reverseStr (s) {\n" +
            "    var result = \"\";\n" +
            "    for (var i = s.length - 1; i > -1; --i) {\n" +
            "        result += s.charAt(i);\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "var hexatrigesimalToChar = [\n" +
            "            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',\n" +
            "            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',\n" +
            "            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',\n" +
            "            'u', 'v', 'w', 'x', 'y', 'z'\n" +
            "        ];\n" +
            "function biToString(x, radix) { // 2 <= radix <= 36\n" +
            "    var b = new BigInt();\n" +
            "    b.digits[0] = radix;\n" +
            "    var qr = biDivideModulo(x, b);\n" +
            "    var result = hexatrigesimalToChar[qr[1].digits[0]];\n" +
            "    while (biCompare(qr[0], bigZero) == 1) {\n" +
            "        qr = biDivideModulo(qr[0], b);\n" +
            "        digit = qr[1].digits[0];\n" +
            "        result += hexatrigesimalToChar[qr[1].digits[0]];\n" +
            "    }\n" +
            "    return (x.isNeg ? \"-\" : \"\") + reverseStr(result);\n" +
            "};\n" +
            "function biToDecimal (x) {\n" +
            "    var b = new BigInt();\n" +
            "    b.digits[0] = 10;\n" +
            "    var qr = biDivideModulo(x, b);\n" +
            "    var result = String(qr[1].digits[0]);\n" +
            "    while (biCompare(qr[0], bigZero) == 1) {\n" +
            "        qr = biDivideModulo(qr[0], b);\n" +
            "        result += String(qr[1].digits[0]);\n" +
            "    }\n" +
            "    return (x.isNeg ? \"-\" : \"\") + reverseStr(result);\n" +
            "};\n" +
            "var hexToChar = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9',\n" +
            "                 'a', 'b', 'c', 'd', 'e', 'f'];\n" +
            "function digitToHex (n) {\n" +
            "    var mask = 0xf;\n" +
            "    var result = \"\";\n" +
            "    for (var i = 0; i < 4; ++i) {\n" +
            "        result += hexToChar[n & mask];\n" +
            "        n >>>= 4;\n" +
            "    }\n" +
            "    return reverseStr(result);\n" +
            "};\n" +
            "function biToHex(x) {\n" +
            "    var result = \"\";\n" +
            "    var n = biHighIndex(x);\n" +
            "    for (var i = biHighIndex(x); i > -1; --i) {\n" +
            "        result += digitToHex(x.digits[i]);\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "function charToHex (c) {\n" +
            "    var ZERO = 48;\n" +
            "    var NINE = ZERO + 9;\n" +
            "    var littleA = 97;\n" +
            "    var littleZ = littleA + 25;\n" +
            "    var bigA = 65;\n" +
            "    var bigZ = 65 + 25;\n" +
            "    var result;\n" +
            "    if (c >= ZERO && c <= NINE) {\n" +
            "        result = c - ZERO;\n" +
            "    } else if (c >= bigA && c <= bigZ) {\n" +
            "        result = 10 + c - bigA;\n" +
            "    } else if (c >= littleA && c <= littleZ) {\n" +
            "        result = 10 + c - littleA;\n" +
            "    } else {\n" +
            "        result = 0;\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "function hexToDigit (s) {\n" +
            "    var result = 0;\n" +
            "    var sl = Math.min(s.length, 4);\n" +
            "    for (var i = 0; i < sl; ++i) {\n" +
            "        result <<= 4;\n" +
            "        result |= charToHex(s.charCodeAt(i));\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "function biFromHex(s) {\n" +
            "    var result = new BigInt();\n" +
            "    var sl = s.length;\n" +
            "    for (var i = sl, j = 0; i > 0; i -= 4, ++j) {\n" +
            "        result.digits[j] = hexToDigit(s.substr(Math.max(i - 4, 0), Math.min(i, 4)));\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "function biFromString (s, radix) {\n" +
            "    var isNeg = s.charAt(0) == '-';\n" +
            "    var istop = isNeg ? 1 : 0;\n" +
            "    var result = new BigInt();\n" +
            "    var place = new BigInt();\n" +
            "    place.digits[0] = 1; // radix^0\n" +
            "    for (var i = s.length - 1; i >= istop; i--) {\n" +
            "        var c = s.charCodeAt(i);\n" +
            "        var digit = charToHex(c);\n" +
            "        var biDigit = biMultiplyDigit(place, digit);\n" +
            "        result = biAdd(result, biDigit);\n" +
            "        place = biMultiplyDigit(place, radix);\n" +
            "    }\n" +
            "    result.isNeg = isNeg;\n" +
            "    return result;\n" +
            "};\n" +
            "function biDump (b) {\n" +
            "    return (b.isNeg ? \"-\" : \"\") + b.digits.join(\" \");\n" +
            "};\n" +
            "function biAdd(x, y) {\n" +
            "    var result;\n" +
            "    if (x.isNeg != y.isNeg) {\n" +
            "        y.isNeg = !y.isNeg;\n" +
            "        result = biSubtract(x, y);\n" +
            "        y.isNeg = !y.isNeg;\n" +
            "    }\n" +
            "    else {\n" +
            "        result = new BigInt();\n" +
            "        var c = 0;\n" +
            "        var n;\n" +
            "        for (var i = 0; i < x.digits.length; ++i) {\n" +
            "            n = x.digits[i] + y.digits[i] + c;\n" +
            "            result.digits[i] = n % biRadix;\n" +
            "            c = Number(n >= biRadix);\n" +
            "        }\n" +
            "        result.isNeg = x.isNeg;\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "function biSubtract (x, y) {\n" +
            "    var result;\n" +
            "    if (x.isNeg != y.isNeg) {\n" +
            "        y.isNeg = !y.isNeg;\n" +
            "        result = biAdd(x, y);\n" +
            "        y.isNeg = !y.isNeg;\n" +
            "    } else {\n" +
            "        result = new BigInt();\n" +
            "        var n, c;\n" +
            "        c = 0;\n" +
            "        for (var i = 0; i < x.digits.length; ++i) {\n" +
            "            n = x.digits[i] - y.digits[i] + c;\n" +
            "            result.digits[i] = n % biRadix;\n" +
            "            // Stupid non-conforming modulus operation.\n" +
            "            if (result.digits[i] < 0) result.digits[i] += biRadix;\n" +
            "            c = 0 - Number(n < 0);\n" +
            "        }\n" +
            "        // Fix up the negative sign, if any.\n" +
            "        if (c == -1) {\n" +
            "            c = 0;\n" +
            "            for (var i = 0; i < x.digits.length; ++i) {\n" +
            "                n = 0 - result.digits[i] + c;\n" +
            "                result.digits[i] = n % biRadix;\n" +
            "                // Stupid non-conforming modulus operation.\n" +
            "                if (result.digits[i] < 0) result.digits[i] += biRadix;\n" +
            "                c = 0 - Number(n < 0);\n" +
            "            }\n" +
            "            // Result is opposite sign of arguments.\n" +
            "            result.isNeg = !x.isNeg;\n" +
            "        } else {\n" +
            "            // Result is same sign.\n" +
            "            result.isNeg = x.isNeg;\n" +
            "        }\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "function biHighIndex (x) {\n" +
            "    var result = x.digits.length - 1;\n" +
            "    while (result > 0 && x.digits[result] == 0) --result;\n" +
            "    return result;\n" +
            "};\n" +
            "function biNumBits (x) {\n" +
            "    var n = biHighIndex(x);\n" +
            "    var d = x.digits[n];\n" +
            "    var m = (n + 1) * bitsPerDigit;\n" +
            "    var result;\n" +
            "    for (result = m; result > m - bitsPerDigit; --result) {\n" +
            "        if ((d & 0x8000) != 0) break;\n" +
            "        d <<= 1;\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "function biMultiply (x, y) {\n" +
            "    var result = new BigInt();\n" +
            "    var c;\n" +
            "    var n = biHighIndex(x);\n" +
            "    var t = biHighIndex(y);\n" +
            "    var u, uv, k;\n" +
            "    for (var i = 0; i <= t; ++i) {\n" +
            "        c = 0;\n" +
            "        k = i;\n" +
            "        for (var j = 0; j <= n; ++j, ++k) {\n" +
            "            uv = result.digits[k] + x.digits[j] * y.digits[i] + c;\n" +
            "            result.digits[k] = uv & maxDigitVal;\n" +
            "            c = uv >>> biRadixBits;\n" +
            "            //c = Math.floor(uv / biRadix);\n" +
            "        }\n" +
            "        result.digits[i + n + 1] = c;\n" +
            "    }\n" +
            "    // Someone give me a logical xor, please.\n" +
            "    result.isNeg = x.isNeg != y.isNeg;\n" +
            "    return result;\n" +
            "};\n" +
            "function biMultiplyDigit (x, y) {\n" +
            "    var n, c, uv;\n" +
            "    var result = new BigInt();\n" +
            "    n = biHighIndex(x);\n" +
            "    c = 0;\n" +
            "    for (var j = 0; j <= n; ++j) {\n" +
            "        uv = result.digits[j] + x.digits[j] * y + c;\n" +
            "        result.digits[j] = uv & maxDigitVal;\n" +
            "        c = uv >>> biRadixBits;\n" +
            "        //c = Math.floor(uv / biRadix);\n" +
            "    }\n" +
            "    result.digits[1 + n] = c;\n" +
            "    return result;\n" +
            "};\n" +
            "function arrayCopy (src, srcStart, dest, destStart, n) {\n" +
            "    var m = Math.min(srcStart + n, src.length);\n" +
            "    for (var i = srcStart, j = destStart; i < m; ++i, ++j) {\n" +
            "        dest[j] = src[i];\n" +
            "    }\n" +
            "};\n" +
            "var highBitMasks = [0x0000, 0x8000, 0xC000, 0xE000, 0xF000, 0xF800,\n" +
            "                    0xFC00, 0xFE00, 0xFF00, 0xFF80, 0xFFC0, 0xFFE0,\n" +
            "                    0xFFF0, 0xFFF8, 0xFFFC, 0xFFFE, 0xFFFF];\n" +
            "function biShiftLeft (x, n) {\n" +
            "    var digitCount = Math.floor(n / bitsPerDigit);\n" +
            "    var result = new BigInt();\n" +
            "    arrayCopy(x.digits, 0, result.digits, digitCount,\n" +
            "              result.digits.length - digitCount);\n" +
            "    var bits = n % bitsPerDigit;\n" +
            "    var rightBits = bitsPerDigit - bits;\n" +
            "    for (var i = result.digits.length - 1, i1 = i - 1; i > 0; --i, --i1) {\n" +
            "        result.digits[i] = ((result.digits[i] << bits) & maxDigitVal) |\n" +
            "                ((result.digits[i1] & highBitMasks[bits]) >>>\n" +
            "                 (rightBits));\n" +
            "    }\n" +
            "    result.digits[0] = ((result.digits[i] << bits) & maxDigitVal);\n" +
            "    result.isNeg = x.isNeg;\n" +
            "    return result;\n" +
            "};\n" +
            "var lowBitMasks = [0x0000, 0x0001, 0x0003, 0x0007, 0x000F, 0x001F,\n" +
            "                   0x003F, 0x007F, 0x00FF, 0x01FF, 0x03FF, 0x07FF,\n" +
            "                   0x0FFF, 0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF];\n" +
            "function biShiftRight (x, n) {\n" +
            "    var digitCount = Math.floor(n / bitsPerDigit);\n" +
            "    var result = new BigInt();\n" +
            "    arrayCopy(x.digits, digitCount, result.digits, 0,\n" +
            "              x.digits.length - digitCount);\n" +
            "    var bits = n % bitsPerDigit;\n" +
            "    var leftBits = bitsPerDigit - bits;\n" +
            "    for (var i = 0, i1 = i + 1; i < result.digits.length - 1; ++i, ++i1) {\n" +
            "        result.digits[i] = (result.digits[i] >>> bits) |\n" +
            "                ((result.digits[i1] & lowBitMasks[bits]) << leftBits);\n" +
            "    }\n" +
            "    result.digits[result.digits.length - 1] >>>= bits;\n" +
            "    result.isNeg = x.isNeg;\n" +
            "    return result;\n" +
            "};\n" +
            "function biMultiplyByRadixPower (x, n) {\n" +
            "    var result = new BigInt();\n" +
            "    arrayCopy(x.digits, 0, result.digits, n, result.digits.length - n);\n" +
            "    return result;\n" +
            "};\n" +
            "function biDivideByRadixPower (x, n) {\n" +
            "    var result = new BigInt();\n" +
            "    arrayCopy(x.digits, n, result.digits, 0, result.digits.length - n);\n" +
            "    return result;\n" +
            "};\n" +
            "function biModuloByRadixPower (x, n) {\n" +
            "    var result = new BigInt();\n" +
            "    arrayCopy(x.digits, 0, result.digits, 0, n);\n" +
            "    return result;\n" +
            "};\n" +
            "function biCompare (x, y) {\n" +
            "    if (x.isNeg != y.isNeg) {\n" +
            "        return 1 - 2 * Number(x.isNeg);\n" +
            "    }\n" +
            "    for (var i = x.digits.length - 1; i >= 0; --i) {\n" +
            "        if (x.digits[i] != y.digits[i]) {\n" +
            "            if (x.isNeg) {\n" +
            "                return 1 - 2 * Number(x.digits[i] > y.digits[i]);\n" +
            "            } else {\n" +
            "                return 1 - 2 * Number(x.digits[i] < y.digits[i]);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    return 0;\n" +
            "};\n" +
            "\n" +
            "function biDivideModulo(x, y) {\n" +
            "    var nb = biNumBits(x);\n" +
            "    var tb = biNumBits(y);\n" +
            "    var origYIsNeg = y.isNeg;\n" +
            "    var q, r;\n" +
            "    if (nb < tb) {\n" +
            "        // |x| < |y|\n" +
            "        if (x.isNeg) {\n" +
            "            q = biCopy(bigOne);\n" +
            "            q.isNeg = !y.isNeg;\n" +
            "            x.isNeg = false;\n" +
            "            y.isNeg = false;\n" +
            "            r = biSubtract(y, x);\n" +
            "            // Restore signs, 'cause they're references.\n" +
            "            x.isNeg = true;\n" +
            "            y.isNeg = origYIsNeg;\n" +
            "        } else {\n" +
            "            q = new BigInt();\n" +
            "            r = biCopy(x);\n" +
            "        }\n" +
            "        return [q, r];\n" +
            "    }\n" +
            "\n" +
            "    q = new BigInt();\n" +
            "    r = x;\n" +
            "\n" +
            "    // Normalize Y.\n" +
            "    var t = Math.ceil(tb / bitsPerDigit) - 1;\n" +
            "    var lambda = 0;\n" +
            "    while (y.digits[t] < biHalfRadix) {\n" +
            "        y = biShiftLeft(y, 1);\n" +
            "        ++lambda;\n" +
            "        ++tb;\n" +
            "        t = Math.ceil(tb / bitsPerDigit) - 1;\n" +
            "    }\n" +
            "    // Shift r over to keep the quotient constant. We'll shift the\n" +
            "    // remainder back at the end.\n" +
            "    r = biShiftLeft(r, lambda);\n" +
            "    nb += lambda; // Update the bit count for x.\n" +
            "    var n = Math.ceil(nb / bitsPerDigit) - 1;\n" +
            "\n" +
            "    var b = biMultiplyByRadixPower(y, n - t);\n" +
            "    while (biCompare(r, b) != -1) {\n" +
            "        ++q.digits[n - t];\n" +
            "        r = biSubtract(r, b);\n" +
            "    }\n" +
            "    for (var i = n; i > t; --i) {\n" +
            "        var ri = (i >= r.digits.length) ? 0 : r.digits[i];\n" +
            "        var ri1 = (i - 1 >= r.digits.length) ? 0 : r.digits[i - 1];\n" +
            "        var ri2 = (i - 2 >= r.digits.length) ? 0 : r.digits[i - 2];\n" +
            "        var yt = (t >= y.digits.length) ? 0 : y.digits[t];\n" +
            "        var yt1 = (t - 1 >= y.digits.length) ? 0 : y.digits[t - 1];\n" +
            "        if (ri == yt) {\n" +
            "            q.digits[i - t - 1] = maxDigitVal;\n" +
            "        } else {\n" +
            "            q.digits[i - t - 1] = Math.floor((ri * biRadix + ri1) / yt);\n" +
            "        }\n" +
            "\n" +
            "        var c1 = q.digits[i - t - 1] * ((yt * biRadix) + yt1);\n" +
            "        var c2 = (ri * biRadixSquared) + ((ri1 * biRadix) + ri2);\n" +
            "        while (c1 > c2) {\n" +
            "            --q.digits[i - t - 1];\n" +
            "            c1 = q.digits[i - t - 1] * ((yt * biRadix) | yt1);\n" +
            "            c2 = (ri * biRadix * biRadix) + ((ri1 * biRadix) + ri2);\n" +
            "        }\n" +
            "\n" +
            "        b = biMultiplyByRadixPower(y, i - t - 1);\n" +
            "        r = biSubtract(r, biMultiplyDigit(b, q.digits[i - t - 1]));\n" +
            "        if (r.isNeg) {\n" +
            "            r = biAdd(r, b);\n" +
            "            --q.digits[i - t - 1];\n" +
            "        }\n" +
            "    }\n" +
            "    r = biShiftRight(r, lambda);\n" +
            "    // Fiddle with the signs and stuff to make sure that 0 <= r < y.\n" +
            "    q.isNeg = x.isNeg != origYIsNeg;\n" +
            "    if (x.isNeg) {\n" +
            "        if (origYIsNeg) {\n" +
            "            q = biAdd(q, bigOne);\n" +
            "        } else {\n" +
            "            q = biSubtract(q, bigOne);\n" +
            "        }\n" +
            "        y = biShiftRight(y, lambda);\n" +
            "        r = biSubtract(y, r);\n" +
            "    }\n" +
            "    // Check for the unbelievably stupid degenerate case of r == -0.\n" +
            "    if (r.digits[0] == 0 && biHighIndex(r) == 0) r.isNeg = false;\n" +
            "\n" +
            "    return [q, r];\n" +
            "};\n" +
            "\n" +
            "function biDivide (x, y) {\n" +
            "    return biDivideModulo(x, y)[0];\n" +
            "};\n" +
            "\n" +
            "function biModulo (x, y) {\n" +
            "    return biDivideModulo(x, y)[1];\n" +
            "};\n" +
            "\n" +
            "function biMultiplyMod (x, y, m) {\n" +
            "    return biModulo(biMultiply(x, y), m);\n" +
            "};\n" +
            "\n" +
            "function biPow (x, y) {\n" +
            "    var result = bigOne;\n" +
            "    var a = x;\n" +
            "    while (true) {\n" +
            "        if ((y & 1) != 0) result = biMultiply(result, a);\n" +
            "        y >>= 1;\n" +
            "        if (y == 0) break;\n" +
            "        a = biMultiply(a, a);\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "\n" +
            "function biPowMod (x, y, m) {\n" +
            "    var result = bigOne;\n" +
            "    var a = x;\n" +
            "    var k = y;\n" +
            "    while (true) {\n" +
            "        if ((k.digits[0] & 1) != 0) result = biMultiplyMod(result, a, m);\n" +
            "        k = biShiftRight(k, 1);\n" +
            "        if (k.digits[0] == 0 && biHighIndex(k) == 0) break;\n" +
            "        a = biMultiplyMod(a, a, m);\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "\n" +
            "\n" +
            "function BarrettMu(m) {\n" +
            "    this.modulus = biCopy(m);\n" +
            "    this.k = biHighIndex(this.modulus) + 1;\n" +
            "    var b2k = new BigInt();\n" +
            "    b2k.digits[2 * this.k] = 1; // b2k = b^(2k)\n" +
            "    this.mu = biDivide(b2k, this.modulus);\n" +
            "    this.bkplus1 = new BigInt();\n" +
            "    this.bkplus1.digits[this.k + 1] = 1; // bkplus1 = b^(k+1)\n" +
            "    this.modulo = BarrettMu_modulo;\n" +
            "    this.multiplyMod = BarrettMu_multiplyMod;\n" +
            "    this.powMod = BarrettMu_powMod;\n" +
            "};\n" +
            "\n" +
            "function BarrettMu_modulo(x) {\n" +
            "    var q1 = biDivideByRadixPower(x, this.k - 1);\n" +
            "    var q2 = biMultiply(q1, this.mu);\n" +
            "    var q3 = biDivideByRadixPower(q2, this.k + 1);\n" +
            "    var r1 = biModuloByRadixPower(x, this.k + 1);\n" +
            "    var r2term = biMultiply(q3, this.modulus);\n" +
            "    var r2 = biModuloByRadixPower(r2term, this.k + 1);\n" +
            "    var r = biSubtract(r1, r2);\n" +
            "    if (r.isNeg) {\n" +
            "        r = biAdd(r, this.bkplus1);\n" +
            "    }\n" +
            "    var rgtem = biCompare(r, this.modulus) >= 0;\n" +
            "    while (rgtem) {\n" +
            "        r = biSubtract(r, this.modulus);\n" +
            "        rgtem = biCompare(r, this.modulus) >= 0;\n" +
            "    }\n" +
            "    return r;\n" +
            "}\n" +
            "\n" +
            "function BarrettMu_multiplyMod(x, y) {\n" +
            "    /*\n" +
            "    x = this.modulo(x);\n" +
            "    y = this.modulo(y);\n" +
            "    */\n" +
            "    var xy = biMultiply(x, y);\n" +
            "    return this.modulo(xy);\n" +
            "}\n" +
            "\n" +
            "function BarrettMu_powMod(x, y) {\n" +
            "    var result = new BigInt();\n" +
            "    result.digits[0] = 1;\n" +
            "    var a = x;\n" +
            "    var k = y;\n" +
            "    while (true) {\n" +
            "        if ((k.digits[0] & 1) != 0) result = this.multiplyMod(result, a);\n" +
            "        k = biShiftRight(k, 1);\n" +
            "        if (k.digits[0] == 0 && biHighIndex(k) == 0) break;\n" +
            "        a = this.multiplyMod(a, a);\n" +
            "    }\n" +
            "    return result;\n" +
            "}\n" +
            "\n" +
            "function RSAKeyPair (encryptionExponent, decryptionExponent, modulus) {\n" +
            "    this.e = biFromHex(encryptionExponent);\n" +
            "    this.d = biFromHex(decryptionExponent);\n" +
            "    this.m = biFromHex(modulus);\n" +
            "    this.chunkSize = 2 * biHighIndex(this.m);\n" +
            "    this.radix = 16;\n" +
            "    this.barrett = new BarrettMu(this.m);\n" +
            "};\n" +
            "\n" +
            "function getKeyPair (encryptionExponent, decryptionExponent, modulus) {\n" +
            "    return new RSAKeyPair(encryptionExponent, decryptionExponent, modulus);\n" +
            "};\n" +
            "\n" +
            "function twoDigit(n) {\n" +
            "    return (n < 10 ? \"0\" : \"\") + String(n);\n" +
            "};\n" +
            "\n" +
            "function encryptedString (key, s) {\n" +
            "    var a = [];\n" +
            "    var sl = s.length;\n" +
            "    var i = 0;\n" +
            "    while (i < sl) {\n" +
            "        a[i] = s.charCodeAt(i);\n" +
            "        i++;\n" +
            "    }\n" +
            "\n" +
            "    while (a.length % key.chunkSize != 0) {\n" +
            "        a[i++] = 0;\n" +
            "    }\n" +
            "\n" +
            "    var al = a.length;\n" +
            "    var result = \"\";\n" +
            "    var j, k, block;\n" +
            "    for (i = 0; i < al; i += key.chunkSize) {\n" +
            "        block = new BigInt();\n" +
            "        j = 0;\n" +
            "        for (k = i; k < i + key.chunkSize; ++j) {\n" +
            "            block.digits[j] = a[k++];\n" +
            "            block.digits[j] += a[k++] << 8;\n" +
            "        }\n" +
            "        var crypt = key.barrett.powMod(block, key.e);\n" +
            "        var text = key.radix == 16 ? biToHex(crypt) : biToString(crypt, key.radix);\n" +
            "        result += text + \" \";\n" +
            "    }\n" +
            "    return result.substring(0, result.length - 1); // Remove last space.\n" +
            "};\n" +
            "\n" +
            "function decryptedString (key, s) {\n" +
            "    var blocks = s.split(\" \");\n" +
            "    var result = \"\";\n" +
            "    var i, j, block;\n" +
            "    for (i = 0; i < blocks.length; ++i) {\n" +
            "        var bi;\n" +
            "        if (key.radix == 16) {\n" +
            "            bi = biFromHex(blocks[i]);\n" +
            "        }\n" +
            "        else {\n" +
            "            bi = biFromString(blocks[i], key.radix);\n" +
            "        }\n" +
            "        block = key.barrett.powMod(bi, key.d);\n" +
            "        for (j = 0; j <= biHighIndex(block); ++j) {\n" +
            "            result += String.fromCharCode(block.digits[j] & 255,\n" +
            "                                          block.digits[j] >> 8);\n" +
            "        }\n" +
            "    }\n" +
            "    // Remove trailing null, if any.\n" +
            "    if (result.charCodeAt(result.length - 1) == 0) {\n" +
            "        result = result.substring(0, result.length - 1);\n" +
            "    }\n" +
            "    return result;\n" +
            "};\n" +
            "\n" +
            "setMaxDigits(130);\n" +
            "\n" +
            "function encryption(pwd){\n" +
            "    var modulus = \"00b6b7f8531b19980c66ae08e3061c6295a1dfd9406b32b202a59737818d75dea03de45d44271a1473af8062e8a4df927f031668ba0b1ec80127ff323a24cd0100bef4d524fdabef56271b93146d64589c9a988b67bc1d7a62faa6c378362cfd0a875361ddc7253aa0c0085dd5b17029e179d64294842862e6b0981ca1bde29979\";\n" +
            "    var exponent = \"010001\";\n" +
            "    var key = getKeyPair(exponent,\"\", modulus);\n" +
            "    result = encryptedString(key, pwd);\n" +
            "    return result;\n" +
            "}";
}
