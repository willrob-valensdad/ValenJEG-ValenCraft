#!/usr/bin/env python3
"""Stitch Fabric @Actual members into common @Expect declarations.

Upstream sources stay untouched. Generated sources are what Fabric Loom compiles.
"""
import sys
from pathlib import Path

def strip_comments_and_strings(text):
    """Return a mask string of the same length; braces outside code become spaces."""
    out = list(text)
    i = 0
    n = len(text)
    while i < n:
        if text.startswith('//', i):
            j = text.find('\n', i)
            if j < 0:
                j = n
            for k in range(i, j):
                if out[k] in '{}':
                    out[k] = ' '
            i = j
        elif text.startswith('/*', i):
            j = text.find('*/', i+2)
            if j < 0:
                j = n
            else:
                j += 2
            for k in range(i, j):
                if out[k] in '{}':
                    out[k] = ' '
            i = j
        elif text[i] == '"':
            i += 1
            while i < n:
                if text[i] == '\\':
                    i += 2
                    continue
                if text[i] == '"':
                    i += 1
                    break
                if out[i] in '{}':
                    out[i] = ' '
                i += 1
        elif text[i] == "'":
            i += 1
            while i < n:
                if text[i] == '\\':
                    i += 2
                    continue
                if text[i] == "'":
                    i += 1
                    break
                i += 1
        else:
            i += 1
    return ''.join(out)

def class_span(masked, original):
    # first top-level type
    for kind in ('class ', 'interface ', 'enum '):
        idx = 0
        while True:
            at = masked.find(kind, idx)
            if at < 0:
                break
            # must be a declaration: previous nonspace is not '.' 
            prev = masked[:at].rstrip()
            if prev.endswith('.') or prev.endswith('@'):
                idx = at + 1
                continue
            # brace of this type
            brace = masked.find('{', at)
            if brace < 0:
                break
            depth = 0
            for j in range(brace, len(masked)):
                if masked[j] == '{':
                    depth += 1
                elif masked[j] == '}':
                    depth -= 1
                    if depth == 0:
                        name = original[at+len(kind):brace].split('<',1)[0].split(' ',1)[0].strip()
                        return name, brace, j
            break
    raise ValueError('no type')

def split_members(masked_body, original_body):
    # body includes surrounding braces
    members = []
    i = 1
    n = len(masked_body) - 1
    while i < n:
        while i < n and masked_body[i].isspace():
            i += 1
        if i >= n:
            break
        start = i
        depth = 0
        while i < n:
            c = masked_body[i]
            if c == '{':
                depth += 1
            elif c == '}':
                if depth == 0:
                    break
                depth -= 1
                if depth == 0:
                    i += 1
                    members.append(original_body[start:i])
                    break
            elif c == ';' and depth == 0:
                i += 1
                members.append(original_body[start:i])
                break
            i += 1
        else:
            break
    return members

def sig_key(member, class_names):
    # drop annotations and comments for signature
    lines = []
    for ln in member.splitlines():
        s = ln.strip()
        if not s or s.startswith('@') or s.startswith('//') or s.startswith('*') or s.startswith('/*'):
            continue
        lines.append(s)
    if not lines:
        return None
    sig = ' '.join(lines)
    for word in ('public', 'protected', 'private', 'static', 'final', 'default', 'synchronized', 'native'):
        sig = sig.replace(word + ' ', '')
    angle = paren = 0
    split_at = None
    split_kind = None
    for i, ch in enumerate(sig):
        if ch == '<':
            angle += 1
        elif ch == '>':
            angle = max(0, angle-1)
        elif ch == '(' and angle == 0:
            split_at = i
            split_kind = 'call'
            break
        elif ch == '=' and angle == 0 and paren == 0:
            split_at = i
            split_kind = 'field'
            break
    if split_kind == 'field' or '(' not in sig:
        left = sig.split('=', 1)[0].strip().rstrip(';')
        parts = left.split()
        if not parts:
            return None
        return ('field', parts[-1])
    head = sig.split('(', 1)[0].strip()
    name = head.split()[-1]
    params = sig.split('(', 1)[1]
    depth = 0
    buf = []
    for ch in params:
        if ch == '(':
            depth += 1
        elif ch == ')':
            if depth == 0:
                break
            depth -= 1
        buf.append(ch)
    plist = ''.join(buf)
    depth = 0
    commas = 0
    nonempty = False
    for ch in plist:
        if ch in '<([':
            depth += 1
        elif ch in '>)]':
            depth = max(0, depth-1)
        elif ch == ',' and depth == 0:
            commas += 1
        elif not ch.isspace():
            nonempty = True
    pcount = commas + 1 if nonempty else 0
    if name in class_names:
        return ('ctor', pcount)
    return ('method', name, pcount)

def clean_member(member, actual_name, expect_name):
    lines = []
    for ln in member.splitlines(keepends=True):
        if ln.strip() == '@Actual':
            continue
        lines.append(ln.replace(actual_name, expect_name))
    text = ''.join(lines)
    if not text.endswith('\n'):
        text += '\n'
    return text

def extra_imports(expect_text, actual_text, stitched):
    have = set()
    for ln in stitched.splitlines():
        s = ln.strip()
        if s.startswith('import '):
            have.add(s)
    add = []
    for ln in actual_text.splitlines():
        s = ln.strip()
        if s.startswith('import ') and 'multiplatform' not in s and s not in have:
            # only if simple name is referenced
            simple = s[len('import '):].rstrip(';').split('.')[-1]
            if simple in stitched:
                add.append(s)
    if not add:
        return stitched
    # insert after package
    lines = stitched.splitlines(keepends=True)
    for i, ln in enumerate(lines):
        if ln.startswith('package '):
            block = ''.join(a + '\n' for a in add)
            lines.insert(i+1, '\n' + block)
            break
    return ''.join(lines)

def stitch_text(expect_text, actual_text):
    em = strip_comments_and_strings(expect_text)
    am = strip_comments_and_strings(actual_text)
    ename, el, er = class_span(em, expect_text)
    aname, al, ar = class_span(am, actual_text)
    emembers = split_members(em[el:er+1], expect_text[el:er+1])
    amembers = split_members(am[al:ar+1], actual_text[al:ar+1])
    amap = {}
    for m in amembers:
        k = sig_key(m, {aname, aname+'Actual', ename})
        if k:
            amap.setdefault(k, m)
    used = set()
    class_level = '@Expect' in expect_text[:el]
    rebuilt = []
    for m in emembers:
        k = sig_key(m, {ename, aname})
        stub = '{' not in strip_comments_and_strings(m)
        if k and k in amap and ('@Expect' in m or (class_level and stub)):
            rebuilt.append(clean_member(amap[k], aname, ename))
            used.add(k)
        else:
            # still drop the annotation if present on a non-matched member
            lines = [ln for ln in m.splitlines(keepends=True) if ln.strip() != '@Expect']
            rebuilt.append(''.join(lines) if lines[-1].endswith('\n') else ''.join(lines)+'\n')
    for k, m in amap.items():
        if k in used:
            continue
        if k[0] == 'field' and 'static' not in m:
            rebuilt.insert(0, clean_member(m, aname, ename))
    inner = ''.join(rebuilt)
    out = expect_text[:el+1] + '\n' + inner + expect_text[er:]
    lines = [ln for ln in out.splitlines(keepends=True) if ln.strip() != '@Expect' and 'multiplatform.annotations.Expect' not in ln]
    out = ''.join(lines)
    return extra_imports(expect_text, actual_text, out)

def main():
    out = Path(sys.argv[1])
    args = sys.argv[2:]
    sep = args.index('--')
    commons = [Path(p) for p in args[:sep]]
    fabrics = [Path(p) for p in args[sep+1:]]
    actuals = {}
    for root in fabrics:
        for path in root.rglob('*Actual.java'):
            base = path.name[:-len('Actual.java')] + '.java'
            key = str(path.parent.relative_to(root) / base)
            actuals[key] = path
    written = 0
    for root in commons:
        for path in root.rglob('*.java'):
            rel = str(path.relative_to(root))
            if rel not in actuals:
                continue
            text = path.read_text()
            if '@Expect' not in text:
                continue
            stitched = stitch_text(text, actuals[rel].read_text())
            dest = out / rel
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_text(stitched)
            written += 1
            print('stitched', rel)
    print('count', written)

if __name__ == '__main__':
    main()
